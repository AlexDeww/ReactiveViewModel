package com.alexdeww.reactiveviewmodel.core

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface RvmComponent {

    fun <T> State<T>.setValue(value: T) {
        this.consumer.accept(value)
    }

    fun <T> State<T>.setValueIfChanged(value: T) {
        if (this.value != value) this.consumer.accept(value)
    }

    val <T> State<T>.consumer: Consumer<T> get() = this.consumer

    val <T> Action<T>.observable: Observable<T> get() = this.observable

    fun <T> Event<T>.call(value: T) {
        this.consumer.accept(value)
    }

    fun Event<Unit>.call() {
        this.consumer.accept(Unit)
    }

    val <T> Event<T>.consumer: Consumer<T> get() = this.consumer

}