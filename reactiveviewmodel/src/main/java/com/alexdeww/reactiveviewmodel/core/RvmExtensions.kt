package com.alexdeww.reactiveviewmodel.core

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
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

fun <T : Any> Event<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun <T : Any> ConfirmationEvent<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun <T : Any> State<T>.observe(
    owner: LifecycleOwner,
    action: OnLiveDataAction<T>
): Observer<T> = liveData.observe(owner = owner, action = action)

fun Action<Unit>.call() = call(Unit)

typealias ActionOnClick = () -> Unit

fun <T : Any> Action<T>.bindOnClick(view: View, value: T, onClickAction: ActionOnClick? = null) {
    view.setOnClickListener {
        call(value)
        onClickAction?.invoke()
    }
}

fun Action<Unit>.bindOnClick(view: View, onClickAction: ActionOnClick? = null) {
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
