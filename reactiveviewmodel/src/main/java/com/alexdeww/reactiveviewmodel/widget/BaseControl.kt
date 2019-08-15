package com.alexdeww.reactiveviewmodel.widget

import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State

abstract class BaseControl : RvmComponent {

    protected fun <T> state(initValue: T? = null): State<T> = State(initValue)

    protected fun <T> event(): Event<T> = Event()

    protected fun eventNone(): Event<Unit> = Event()

    protected fun <T> action(): Action<T> = Action()

    protected fun actionNone(): Action<Unit> = Action()

}