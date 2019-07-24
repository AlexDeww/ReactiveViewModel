package com.alexdeww.reactiveviewmodel.core

import io.reactivex.Observable
import io.reactivex.functions.Consumer

abstract class BaseControl {

    protected fun <T> State<T>.setValue(value: T) {
        this.consumer.accept(value)
    }

    protected fun <T> State<T>.setValueIfChanged(value: T) {
        if (this.value != value) this.consumer.accept(value)
    }

    protected val <T> State<T>.consumer: Consumer<T> get() = this.consumer

    protected val <T> Action<T>.observable: Observable<T> get() = this.observable

    protected fun <T> Event<T>.call(value: T) {
        this.consumer.accept(value)
    }

    protected fun Event<Unit>.call() {
        this.consumer.accept(Unit)
    }

    protected val <T> Event<T>.consumer: Consumer<T> get() = this.consumer

    protected fun <T> state(initValue: T? = null): State<T> = State(initValue)
    protected fun <T> event(): Event<T> = Event()
    protected fun emptyEvent(): Event<Unit> = Event()
    protected fun <T> action(): Action<T> = Action()
    protected fun emptyAction(): Action<Unit> = Action()

}