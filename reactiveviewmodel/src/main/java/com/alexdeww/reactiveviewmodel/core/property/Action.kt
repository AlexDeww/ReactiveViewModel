package com.alexdeww.reactiveviewmodel.core.property

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject

class Action<T> internal constructor() {

    private val subject = PublishSubject.create<T>().toSerialized()

    internal val observable: Observable<T> = subject

    val consumer: Consumer<T> = Consumer { subject.onNext(it) }

    fun call(value: T) {
        consumer.accept(value)
    }

}