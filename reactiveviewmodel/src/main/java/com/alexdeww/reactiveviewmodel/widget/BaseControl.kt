package com.alexdeww.reactiveviewmodel.widget

import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State

abstract class BaseControl : RvmComponent {

    protected fun <T> state(initValue: T? = null, debounceInterval: Long? = null): State<T> =
        State(initValue, debounceInterval)

    protected fun <T> event(debounceInterval: Long? = null): Event<T> = Event(debounceInterval)

    protected fun eventNone(debounceInterval: Long? = null): Event<Unit> = Event(debounceInterval)

    protected fun <T> action(debounceInterval: Long? = null): Action<T> = Action(debounceInterval)

    protected fun actionNone(debounceInterval: Long? = null): Action<Unit> =
        Action(debounceInterval)

}
