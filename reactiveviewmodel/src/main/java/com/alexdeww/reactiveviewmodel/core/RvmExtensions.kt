package com.alexdeww.reactiveviewmodel.core

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.RvmAction
import com.alexdeww.reactiveviewmodel.core.property.RvmConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Function
import java.util.concurrent.atomic.AtomicBoolean

typealias OnLiveDataAction<T> = (data: T) -> Unit

fun <T> LiveData<T>.observe(owner: LifecycleOwner, action: OnLiveDataAction<T>): Observer<T> {
    val observer = Observer<T> { if (it != null) action(it) }
    observe(owner, observer)
    return observer
}

fun <T : Any> RvmEvent<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun <T : Any> RvmConfirmationEvent<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun <T : Any> RvmState<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun RvmAction<Unit>.call() = call(Unit)

typealias ActionOnClick = () -> Unit

fun <T : Any> RvmAction<T>.bindOnClick(view: View, value: T, onClickAction: ActionOnClick? = null) {
    view.setOnClickListener {
        call(value)
        onClickAction?.invoke()
    }
}

fun RvmAction<Unit>.bindOnClick(view: View, onClickAction: ActionOnClick? = null) {
    bindOnClick(view, Unit, onClickAction)
}

fun <T : Any> Observable<T>.bindProgress(progressConsumer: Consumer<Boolean>): Observable<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T : Any> Flowable<T>.bindProgress(progressConsumer: Consumer<Boolean>): Flowable<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T : Any> Single<T>.bindProgress(progressConsumer: Consumer<Boolean>): Single<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T : Any> Maybe<T>.bindProgress(progressConsumer: Consumer<Boolean>): Maybe<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun Completable.bindProgress(progressConsumer: Consumer<Boolean>): Completable = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

private open class FinallyConsumerWrapper<T : Any>(
    private val consumer: Consumer<T>
) {
    private val isFinally = AtomicBoolean(false)

    fun reset(value: T) {
        isFinally.set(false)
        consumer.accept(value)
    }

    fun finally(value: T) {
        if (isFinally.compareAndSet(false, true)) consumer.accept(value)
    }
}

private class ProgressConsumerWrapper(
    consumer: Consumer<Boolean>
) : FinallyConsumerWrapper<Boolean>(consumer) {
    fun begin() = reset(true)
    fun end() = finally(false)
}

fun <T : Any> Observable<T>.bindProgressAny(progressConsumer: Consumer<Boolean>): Observable<T> {
    val consumerWrapper = ProgressConsumerWrapper(progressConsumer)
    return this
        .doOnSubscribe { consumerWrapper.begin() }
        .doOnNext { consumerWrapper.end() }
        .doOnComplete { consumerWrapper.end() }
        .doOnError { consumerWrapper.end() }
        .doOnDispose { consumerWrapper.end() }
}

fun <T : Any> Single<T>.bindProgressAny(progressConsumer: Consumer<Boolean>): Single<T> {
    val consumerWrapper = ProgressConsumerWrapper(progressConsumer)
    return this
        .doOnSubscribe { consumerWrapper.begin() }
        .doOnSuccess { consumerWrapper.end() }
        .doOnError { consumerWrapper.end() }
        .doOnDispose { consumerWrapper.end() }
}

fun <T : Any> Maybe<T>.bindProgressAny(progressConsumer: Consumer<Boolean>): Maybe<T> {
    val consumerWrapper = ProgressConsumerWrapper(progressConsumer)
    return this
        .doOnSubscribe { consumerWrapper.begin() }
        .doOnSuccess { consumerWrapper.end() }
        .doOnComplete { consumerWrapper.end() }
        .doOnError { consumerWrapper.end() }
        .doOnDispose { consumerWrapper.end() }
}

fun Completable.bindProgressAny(progressConsumer: Consumer<Boolean>): Completable {
    val consumerWrapper = ProgressConsumerWrapper(progressConsumer)
    return this
        .doOnSubscribe { consumerWrapper.begin() }
        .doOnComplete { consumerWrapper.end() }
        .doOnError { consumerWrapper.end() }
        .doOnDispose { consumerWrapper.end() }
}

fun <T : Any> Observable<T>.untilOn(
    vararg rvmAction: RvmAction<out Any>
): Observable<T> = takeUntil(Observable.merge(rvmAction.map { it.observable }))

fun <T : Any> Observable<T>.untilOn(
    vararg event: RvmEvent<out Any>
): Observable<T> = takeUntil(Observable.merge(event.map { it.observable }))

fun <T : Any> Observable<T>.untilOn(
    vararg observable: Observable<*>
): Observable<T> = takeUntil(Observable.merge(observable.toList()))

fun <T : Any> Maybe<T>.untilOn(
    vararg action: RvmAction<*>
): Maybe<T> = takeUntil(Maybe.merge(action.map { it.observable.firstElement() }))

fun <T : Any> Maybe<T>.untilOn(
    vararg event: RvmEvent<*>
): Maybe<T> = takeUntil(Maybe.merge(event.map { it.observable.firstElement() }))

fun <T : Any> Maybe<T>.untilOn(
    vararg maybe: Maybe<*>
): Maybe<T> = takeUntil(Maybe.merge(maybe.toList()))

fun Completable.untilOn(
    vararg action: RvmAction<*>
): Completable = takeUntil(Completable.merge(action.map {
    it.observable.firstElement().ignoreElement()
}))

fun Completable.untilOn(
    vararg event: RvmEvent<*>
): Completable = takeUntil(Completable.merge(event.map {
    it.observable.firstElement().ignoreElement()
}))

fun Completable.untilOn(
    vararg completable: Completable
): Completable = takeUntil(Completable.merge(completable.toList()))

/**
 * Returns the [Observable] that emits items when active, and buffers them when [idle][isIdle].
 * Buffered items is emitted when idle state ends.
 * @param isIdle shows when the idle state begins (`true`) and ends (`false`).
 * @param bufferSize number of items the buffer can hold. `null` means not constrained.
 */
fun <T : Any> Observable<T>.bufferWhileIdle(
    isIdle: Observable<Boolean>,
    bufferSize: Int? = null
): Observable<T> {
    val itemsObservable = this
        .withLatestFrom(isIdle) { t: T, idle: Boolean -> t to idle }
        .publish()
        .refCount(2)

    return Observable
        .merge(
            itemsObservable
                .filter { (_, isIdle) -> !isIdle }
                .map { (item, _) -> item },

            itemsObservable
                .filter { (_, isIdle) -> isIdle }
                .map { (item, _) -> item }
                .buffer(
                    isIdle
                        .distinctUntilChanged()
                        .filter { it },
                    Function<Boolean, Observable<Boolean>> {
                        isIdle
                            .distinctUntilChanged()
                            .filter { !it }
                    }
                )
                .flatMapIterable { if (bufferSize != null) it.takeLast(bufferSize) else it }
        )
}
