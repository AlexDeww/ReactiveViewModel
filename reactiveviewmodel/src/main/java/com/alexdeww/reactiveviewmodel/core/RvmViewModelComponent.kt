package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.RvmAction
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@RvmBinderDslMarker
interface RvmViewModelComponent : RvmPropertiesSupport, RvmWidgetsSupport,
    RvmAutoDisposableSupport {

    interface Invocable<T> {
        val isExecute: Boolean
        val isExecuteObservable: Observable<Boolean>
        operator fun invoke(params: T)
    }

    // Bind Logic
    fun <T : Any> Observable<T>.applyDefaultErrorHandler(): Observable<T> = this

    @RvmBinderDslMarker
    infix fun <T : Any, R : Any> RvmAction<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<R>
    ) = bindAction(this, transformChainBlock)

    @RvmBinderDslMarker
    infix fun <T : Any> RvmState<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) = bindState(this, transformChainBlock)

}

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.invocable(
    block: (params: T) -> Completable
): ReadOnlyProperty<RvmViewModelComponent, RvmViewModelComponent.Invocable<T>> =
    InvocableDelegate(block)

internal fun <T : Any, R : Any> RvmViewModelComponent.bindAction(
    action: RvmAction<T>,
    transformChainBlock: Observable<T>.() -> Observable<R>
) {
    action.observable
        .transformChainBlock()
        .applyDefaultErrorHandler()
        .retry()
        .subscribe()
        .autoDispose()
}

internal fun <T : Any> RvmViewModelComponent.bindState(
    state: RvmState<T>,
    transformChainBlock: Observable<T>.() -> Observable<out Any>
) {
    state.observable
        .transformChainBlock()
        .applyDefaultErrorHandler()
        .retry()
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
