package com.alexdeww.reactiveviewmodel.core

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import io.reactivex.*
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function

typealias OnLiveDataAction<T> = (data: T) -> Unit

fun <T> LiveData<T>.observe(owner: LifecycleOwner, action: OnLiveDataAction<T>): Observer<T> {
    val observer = Observer<T> { if (it != null) action(it) }
    observe(owner, observer)
    return observer
}

fun <T> Event<T>.observe(owner: LifecycleOwner, action: OnLiveDataAction<T>): Observer<T> =
    liveData.observe(owner, action)

fun <T> State<T>.observe(owner: LifecycleOwner, action: OnLiveDataAction<T>): Observer<T> =
    liveData.observe(owner, action)

fun <T> Observable<T>.bindProgress(progressConsumer: Consumer<Boolean>): Observable<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T> Flowable<T>.bindProgress(progressConsumer: Consumer<Boolean>): Flowable<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T> Single<T>.bindProgress(progressConsumer: Consumer<Boolean>): Single<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T> Maybe<T>.bindProgress(progressConsumer: Consumer<Boolean>): Maybe<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun Completable.bindProgress(progressConsumer: Consumer<Boolean>): Completable = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doFinally { progressConsumer.accept(false) }

fun <T> Single<T>.bindProgressAny(progressConsumer: Consumer<Boolean>): Single<T> = this
    .doOnSubscribe { progressConsumer.accept(true) }
    .doOnSuccess { progressConsumer.accept(false) }
    .doOnError { progressConsumer.accept(false) }

/**
 * Returns the [Observable] that emits items when active, and buffers them when [idle][isIdle].
 * Buffered items is emitted when idle state ends.
 * @param isIdle shows when the idle state begins (`true`) and ends (`false`).
 * @param bufferSize number of items the buffer can hold. `null` means not constrained.
 */
fun <T> Observable<T>.bufferWhileIdle(
    isIdle: Observable<Boolean>,
    bufferSize: Int? = null
): Observable<T> {

    val itemsObservable = this
        .withLatestFrom(
            isIdle,
            BiFunction { t: T, idle: Boolean -> t to idle }
        )
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
                .flatMapIterable {
                    if (bufferSize != null) it.takeLast(bufferSize) else it
                }
        )
}