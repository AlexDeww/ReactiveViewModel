package com.alexdeww.reactiveviewmodel.core.common

import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer

interface RvmComponent {

    fun <T : Any> State<T>.setValue(value: T) {
        this.consumer.accept(value)
    }

    fun <T : Any> State<T>.setValueIfChanged(value: T) {
        if (this.value != value) this.consumer.accept(value)
    }

    val <T : Any> State<T>.consumer: Consumer<T> get() = this.consumer

    val <T : Any> State<T>.observable: Observable<T> get() = this.observable

    val <T : Any> Action<T>.observable: Observable<T> get() = this.observable

    fun <T : Any> Event<T>.call(value: T) = this.consumer.accept(value)

    fun Event<Unit>.call() = this.consumer.accept(Unit)

    val <T : Any> Event<T>.consumer: Consumer<T> get() = this.consumer

    val <T : Any> Event<T>.observable: Observable<T> get() = this.observable

    fun <T : Any> ConfirmationEvent<T>.call(value: T) = this.consumer.accept(value)

    fun ConfirmationEvent<Unit>.call() = this.consumer.accept(Unit)

    val <T : Any> ConfirmationEvent<T>.consumer: Consumer<T> get() = this.consumer

    val <T : Any> ConfirmationEvent<T>.observable: Observable<T> get() = this.observable

}
