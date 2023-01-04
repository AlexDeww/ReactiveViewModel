package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.RvmAction
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

@RvmDslMarker
@RvmBinderDslMarker
interface RvmViewModelComponent : RvmComponentsSupport, RvmWidgetsSupport,
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
fun <T : Any> RvmViewModelComponent.invocable(
    block: (params: T) -> Completable
): Lazy<RvmViewModelComponent.Invocable<T>> = lazy {
    val action by action<T>()
    val isExecuteSubj: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    action bind {
        this.switchMapCompletable { params -> block(params).bindProgress(isExecuteSubj::onNext) }
            .toObservable<Unit>()
    }
    object : RvmViewModelComponent.Invocable<T> {
        override val isExecute: Boolean get() = isExecuteSubj.value ?: false
        override val isExecuteObservable: Observable<Boolean> = isExecuteSubj.serialize()
        override fun invoke(params: T) = action.consumer.accept(params)
    }
}

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

