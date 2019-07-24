package com.alexdeww.reactiveviewmodel.core

abstract class BaseControl : RvmComponent {

    protected fun <T> state(initValue: T? = null): State<T> = State(initValue)
    protected fun <T> event(): Event<T> = Event()
    protected fun <T> action(): Action<T> = Action()

}