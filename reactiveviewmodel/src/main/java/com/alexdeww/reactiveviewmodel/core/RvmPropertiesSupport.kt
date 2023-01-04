package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.RvmAction
import com.alexdeww.reactiveviewmodel.core.property.RvmConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyReadOnlyDelegate
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.util.*
import kotlin.properties.ReadOnlyProperty

@RvmDslMarker
interface RvmPropertiesSupport {

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
fun <T : Any> RvmPropertiesSupport.state(
    initValue: T? = null,
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmState<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmState(initValue, debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmPropertiesSupport.progressState(
    initValue: Boolean? = null,
    debounceInterval: Long = DEF_PROGRESS_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmState<Boolean>> = state(
    initValue = initValue,
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmPropertiesSupport.event(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmEvent<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmEvent(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmPropertiesSupport.eventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmEvent<Unit>> = event(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmPropertiesSupport.confirmationEvent(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmConfirmationEvent<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmConfirmationEvent(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmPropertiesSupport.confirmationEventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmConfirmationEvent<Unit>> = confirmationEvent(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmPropertiesSupport.action(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<T>> = RvmPropertyReadOnlyDelegate(
    property = RvmAction(debounceInterval)
)

@Suppress("unused")
@RvmDslMarker
fun RvmPropertiesSupport.actionNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmPropertiesSupport.debouncedAction(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<T>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun RvmPropertiesSupport.debouncedActionNone(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)
