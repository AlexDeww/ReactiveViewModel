package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Based on RxPM
 * https://github.com/dmdevgo/RxPM
 */

const val DEF_PROGRESS_DEBOUNCE_INTERVAL = 500L //ms
const val DEF_ACTION_DEBOUNCE_INTERVAL = 300L //ms

abstract class ReactiveViewModel : ViewModel(), RvmComponent {

    protected interface Invocable<T> {
        val isExecute: Boolean
        val isExecuteObservable: Observable<Boolean>
        operator fun invoke(params: T)
    }

    private val disposableList = CompositeDisposable()

    override fun onCleared() {
        disposableList.clear()
        super.onCleared()
    }

    fun Disposable.disposeOnCleared(): Disposable {
        disposableList.add(this)
        return this
    }

    protected fun <T : Any> state(
        initValue: T? = null,
        debounceInterval: Long? = null
    ): State<T> = State(initValue, debounceInterval)

    protected fun progressState(
        initValue: Boolean? = null,
        debounceInterval: Long = DEF_PROGRESS_DEBOUNCE_INTERVAL
    ): State<Boolean> = state(initValue, debounceInterval)

    protected fun <T : Any> event(debounceInterval: Long? = null): Event<T> =
        Event(debounceInterval)

    protected fun eventNone(debounceInterval: Long? = null): Event<Unit> =
        event(debounceInterval)

    protected fun <T : Any> confirmationEvent(
        debounceInterval: Long? = null
    ): ConfirmationEvent<T> = ConfirmationEvent(debounceInterval)

    protected fun confirmationEventNone(
        debounceInterval: Long? = null
    ): ConfirmationEvent<Unit> = confirmationEvent(debounceInterval)

    protected fun <T : Any> action(debounceInterval: Long? = null): Action<T> =
        Action(debounceInterval)

    protected fun actionNone(debounceInterval: Long? = null): Action<Unit> =
        action(debounceInterval)

    protected fun <T : Any> debouncedAction(
        debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
    ): Action<T> = action(debounceInterval)

    protected fun debouncedActionNone(
        debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
    ): Action<Unit> = action(debounceInterval)

    protected fun <T : Any, R : Any> Action<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<R>
    ) {
        observable
            .transformChainBlock()
            .applyDefaultErrorHandler()
            .retry()
            .subscribe()
            .disposeOnCleared()
    }

    protected fun <T : Any> State<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) {
        observable
            .transformChainBlock()
            .applyDefaultErrorHandler()
            .retry()
            .subscribe()
            .disposeOnCleared()
    }

    protected fun <T : Any> invocable(
        block: (params: T) -> Completable
    ): Lazy<Invocable<T>> = lazy {
        val action = action<T>()
        val isExecuteSubj: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
        action.bind {
            this.switchMapCompletable { params -> block(params).bindProgress(isExecuteSubj::onNext) }
                .toObservable<Unit>()
        }
        object : Invocable<T> {
            override val isExecute: Boolean get() = isExecuteSubj.value ?: false
            override val isExecuteObservable: Observable<Boolean> = isExecuteSubj.serialize()

            override fun invoke(params: T) = action.consumer.accept(params)
        }
    }

    protected fun <T : Any> Observable<T>.untilOn(vararg action: Action<out Any>): Observable<T> =
        takeUntil(Observable.merge(action.map { it.observable }))

    protected fun <T : Any> Observable<T>.untilOn(vararg event: Event<out Any>): Observable<T> =
        takeUntil(Observable.merge(event.map { it.observable }))

    protected fun <T : Any> Observable<T>.untilOn(vararg observable: Observable<*>): Observable<T> =
        takeUntil(Observable.merge(observable.toList()))

    protected fun <T : Any> Maybe<T>.untilOn(vararg action: Action<*>): Maybe<T> =
        takeUntil(Maybe.merge(action.map { it.observable.firstElement() }))

    protected fun <T : Any> Maybe<T>.untilOn(vararg event: Event<*>): Maybe<T> =
        takeUntil(Maybe.merge(event.map { it.observable.firstElement() }))

    protected fun <T : Any> Maybe<T>.untilOn(vararg maybe: Maybe<*>): Maybe<T> =
        takeUntil(Maybe.merge(maybe.toList()))

    protected fun Completable.untilOn(vararg action: Action<*>): Completable =
        takeUntil(Completable.merge(action.map { it.observable.firstElement().ignoreElement() }))

    protected fun Completable.untilOn(vararg event: Event<*>): Completable =
        takeUntil(Completable.merge(event.map { it.observable.firstElement().ignoreElement() }))

    protected fun Completable.untilOn(vararg completable: Completable): Completable =
        takeUntil(Completable.merge(completable.toList()))

    protected open fun <T : Any> Observable<T>.applyDefaultErrorHandler(): Observable<T> = this

}
