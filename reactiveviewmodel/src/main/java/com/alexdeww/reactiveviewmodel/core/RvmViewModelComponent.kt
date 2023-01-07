package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.RvmAction
import com.alexdeww.reactiveviewmodel.core.property.RvmProperty
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import com.alexdeww.reactiveviewmodel.core.property.RvmStateProjection
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyDelegate
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@RvmBinderDslMarker
interface RvmViewModelComponent : RvmPropertiesSupport, RvmAutoDisposableSupport {

    interface Invocable<T> {
        val isExecute: Boolean
        val isExecuteObservable: Observable<Boolean>
        operator fun invoke(params: T)
    }

    // Bind Logic
    fun <T : Any> Observable<T>.applyDefaultErrorHandler(): Observable<T> = this

    @RvmBinderDslMarker
    infix fun <T : Any> RvmAction<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) = bindProperty(this, transformChainBlock)

    @RvmBinderDslMarker
    infix fun <T : Any> RvmState<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) = bindProperty(this, transformChainBlock)

}

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.invocable(
    block: (params: T) -> Completable
): ReadOnlyProperty<RvmViewModelComponent, RvmViewModelComponent.Invocable<T>> =
    InvocableDelegate(block)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.stateProjectionFromSource(
    initialValue: T? = null,
    sourceBlock: () -> Observable<T>
): ReadOnlyProperty<RvmViewModelComponent, RvmStateProjection<T>> = RvmPropertyDelegate.def {
    val projection = RvmStateProjection(initialValue)
    sourceBlock()
        .applyDefaultErrorHandler()
        .retry()
        .subscribe(projection.consumer)
        .autoDispose()
    projection
}

internal fun <T : Any> RvmViewModelComponent.bindProperty(
    rvmProperty: RvmProperty<T>,
    transformChainBlock: Observable<T>.() -> Observable<out Any>
) {
    // 1 - need skip, 2 - has value (skip only if source has value)
    val skipState = AtomicInteger(0)
    val source = rvmProperty.observable
        .replay(1)
        .apply { connect().autoDispose() }
        .doOnNext { skipState.compareAndSet(0, 2) }
        .skipWhile { skipState.compareAndSet(1, 2) }
        .transformChainBlock()
        .doOnError { skipState.compareAndSet(2, 1) }

    Observable
        .fromCallable { 0 }
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap { source.applyDefaultErrorHandler().retry() }
        .subscribe()
        .autoDispose()
}

private class InvocableDelegate<T : Any>(
    private val block: (params: T) -> Completable
) : ReadOnlyProperty<RvmViewModelComponent, RvmViewModelComponent.Invocable<T>> {

    private var value: RvmViewModelComponent.Invocable<T>? = null

    override fun getValue(
        thisRef: RvmViewModelComponent,
        property: KProperty<*>
    ): RvmViewModelComponent.Invocable<T> {
        if (value == null) {
            val action = RvmAction<T>()
            val isExecuteSubj: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
            thisRef.run {
                action bind {
                    this.switchMapCompletable { params -> block(params).bindProgress(isExecuteSubj::onNext) }
                        .toObservable<Unit>()
                }
            }
            value = object : RvmViewModelComponent.Invocable<T> {
                override val isExecute: Boolean get() = isExecuteSubj.value ?: false
                override val isExecuteObservable: Observable<Boolean> = isExecuteSubj.serialize()
                override fun invoke(params: T) = action.consumer.accept(params)
            }
        }
        return value!!
    }

}
