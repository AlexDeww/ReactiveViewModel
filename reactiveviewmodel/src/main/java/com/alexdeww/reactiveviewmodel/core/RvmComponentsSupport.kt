package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.util.*
import kotlin.properties.ReadOnlyProperty

@RvmDslMarker
@RvmBinderDslMarker
interface RvmComponentsSupport {

    // State
    val <T : Any> RvmState<T>.consumer: Consumer<T> get() = this.consumer
    val <T : Any> RvmState<T>.observable: Observable<T> get() = this.observable
    fun <T : Any> RvmState<T>.setValue(value: T) = consumer.accept(value)
    fun <T : Any> RvmState<T>.setValueIfChanged(value: T) {
        if (this.value != value) setValue(value)
    }


    // Event
    val <T : Any> RvmEvent<T>.consumer: Consumer<T> get() = this.consumer
    val <T : Any> RvmEvent<T>.observable: Observable<T> get() = this.observable
    fun <T : Any> RvmEvent<T>.call(value: T) = this.consumer.accept(value)
    fun RvmEvent<Unit>.call() = call(Unit)


    // ConfirmationEvent
    val <T : Any> RvmConfirmationEvent<T>.consumer: Consumer<T> get() = this.consumer
    val <T : Any> RvmConfirmationEvent<T>.observable: Observable<T> get() = this.observable
    fun <T : Any> RvmConfirmationEvent<T>.call(value: T) = this.consumer.accept(value)
    fun RvmConfirmationEvent<Unit>.call() = call(Unit)


    // Action
    val <T : Any> RvmAction<T>.observable: Observable<T> get() = this.observable

}

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmComponentsSupport.state(
    initValue: T? = null,
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmState<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmState(initValue, debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmComponentsSupport.progressState(
    initValue: Boolean? = null,
    debounceInterval: Long = DEF_PROGRESS_DEBOUNCE_INTERVAL
): ReadOnlyProperty<Any?, RvmState<Boolean>> = state(
    initValue = initValue,
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmComponentsSupport.event(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmEvent<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmEvent(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmComponentsSupport.eventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmEvent<Unit>> = event(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmComponentsSupport.confirmationEvent(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmConfirmationEvent<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmConfirmationEvent(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmComponentsSupport.confirmationEventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmConfirmationEvent<Unit>> = confirmationEvent(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmComponentsSupport.action(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmAction<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmAction(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmComponentsSupport.actionNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<Any?, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmComponentsSupport.debouncedAction(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<Any?, RvmAction<T>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun RvmComponentsSupport.debouncedActionNone(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<Any?, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)
