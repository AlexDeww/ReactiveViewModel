package com.alexdeww.reactiveviewmodel.core.property

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

internal fun <T : Any> Observable<T>.letDebounce(debounceInterval: Long?): Observable<T> = when {
    debounceInterval != null && debounceInterval > 0 ->
        this.debounce(debounceInterval, TimeUnit.MILLISECONDS)
    else -> this
}

internal fun <T : Any> Observable<T>.toViewFlowable(): Flowable<T> = this
    .toFlowable(BackpressureStrategy.LATEST)
    .observeOn(AndroidSchedulers.mainThread())
