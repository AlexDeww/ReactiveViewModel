package com.alexdeww.reactiveviewmodel.core.property

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject

class RvmAction<T : Any> internal constructor(debounceInterval: Long? = null) {

    private val subject = PublishSubject.create<T>().toSerialized()

    internal val observable: Observable<T> = subject.letDebounce(debounceInterval)

    val consumer: Consumer<T> = Consumer { subject.onNext(it) }

    fun call(value: T) = consumer.accept(value)

}
