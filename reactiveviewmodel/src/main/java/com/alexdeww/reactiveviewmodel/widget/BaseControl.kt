package com.alexdeww.reactiveviewmodel.widget

import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State

abstract class BaseControl : RvmComponent {

    protected fun <T : Any> state(
        initValue: T? = null,
        debounceInterval: Long? = null
    ): State<T> = State(initValue, debounceInterval)

    protected fun <T : Any> event(debounceInterval: Long? = null): Event<T> =
        Event(debounceInterval)

    protected fun eventNone(debounceInterval: Long? = null): Event<Unit> =
        event(debounceInterval)

    protected fun <T : Any> action(debounceInterval: Long? = null): Action<T> =
        Action(debounceInterval)

    protected fun actionNone(debounceInterval: Long? = null): Action<Unit> =
        action(debounceInterval)

    protected fun <T : Any> confirmationEvent(
        debounceInterval: Long? = null
    ): ConfirmationEvent<T> = ConfirmationEvent(debounceInterval)

    protected fun confirmationEventNone(
        debounceInterval: Long? = null
    ): ConfirmationEvent<Unit> = confirmationEvent(debounceInterval)

}
