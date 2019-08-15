package com.alexdeww.reactiveviewmodel.core.property

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class Action<T> internal constructor() {

    private val subject = PublishSubject.create<T>().toSerialized()

    internal val observable: Observable<T> = subject

    val consumer: Consumer<T> = Consumer { subject.onNext(it) }

    fun call(value: T) {
        consumer.accept(value)
    }

}