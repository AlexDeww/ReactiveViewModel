package com.alexdeww.reactiveviewmodel.core.property

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * В основном предназначен для передачи событий или данных из View во ViewModel.
 *
 * * Не хранит данные.
 */
class RvmAction<T : Any> internal constructor(
    debounceInterval: Long? = null
) : RvmProperty<T>() {

    private val subject = PublishSubject.create<T>().toSerialized()

    override val observable: Observable<T> = subject.letDebounce(debounceInterval)
    public override val consumer: Consumer<T> = Consumer { subject.onNext(it) }

    fun call(value: T) = consumer.accept(value)

}
