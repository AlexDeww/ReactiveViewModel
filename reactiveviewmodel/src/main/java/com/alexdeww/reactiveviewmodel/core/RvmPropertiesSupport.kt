package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.property.*
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyDelegate
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import kotlin.properties.ReadOnlyProperty

@RvmDslMarker
interface RvmPropertiesSupport {

    // common
    val <T : Any> RvmProperty<T>.consumer: Consumer<T> get() = this.consumer
    val <T : Any> RvmPropertyBase<T>.observable: Observable<T> get() = this.observable


    // callable property
    fun <T : Any, R> R.call(value: T) where R : RvmCallableProperty<T>,
                                            R : RvmProperty<T> = consumer.accept(value)

    fun <R> R.call() where R : RvmCallableProperty<Unit>,
                           R : RvmProperty<Unit> = call(Unit)


    // mutable property
    fun <T : Any, R> R.setValue(value: T) where R : RvmMutableValueProperty<T>,
                                                R : RvmProperty<T> = consumer.accept(value)

    fun <T : Any, R> R.setValueIfChanged(value: T) where R : RvmMutableValueProperty<T>,
                                                         R : RvmProperty<T> {
        if (this.value != value) setValue(value)
    }

}

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.state(
    initialValue: T? = null,
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmState<T>> = RvmPropertyDelegate.def {
    RvmState(initialValue, debounceInterval)
}

@Suppress("unused")
@RvmDslMarker
fun <T : Any, R : Any, P> RVM.stateProjection(
    stateSource: P,
    distinctUntilChanged: Boolean = true,
    mapBlock: (value: T) -> R
): ReadOnlyProperty<RvmPropertiesSupport, RvmStateProjection<R>> where P : RvmPropertyBase<T>,
                                                                       P : RvmValueProperty<T> {
    return RvmPropertyDelegate.def {
        val projection = RvmStateProjection<R>()
        val d = stateSource.observable
            .map(mapBlock)
            .run { if (distinctUntilChanged) distinctUntilChanged() else this }
            .subscribe(projection.consumer)
        if (this is RvmAutoDisposableSupport) d.autoDispose()
        projection
    }
}

@Suppress("unused")
@RvmDslMarker
fun RVM.progressState(
    initialValue: Boolean? = null,
    debounceInterval: Long = DEF_PROGRESS_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmState<Boolean>> = state(
    initialValue = initialValue,
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.event(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmEvent<T>> = RvmPropertyDelegate.def {
    RvmEvent(debounceInterval)
}

@Suppress("unused")
@RvmDslMarker
fun RVM.eventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmEvent<Unit>> = event(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.confirmationEvent(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmConfirmationEvent<T>> = RvmPropertyDelegate.def {
    RvmConfirmationEvent(debounceInterval)
}

@Suppress("unused")
@RvmDslMarker
fun RVM.confirmationEventNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmConfirmationEvent<Unit>> = confirmationEvent(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.action(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<T>> = RvmPropertyDelegate.def {
    RvmAction(debounceInterval)
}

@Suppress("unused")
@RvmDslMarker
fun RVM.actionNone(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.debouncedAction(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<T>> = action(
    debounceInterval = debounceInterval
)

@Suppress("unused")
@RvmDslMarker
fun RVM.debouncedActionNone(
    debounceInterval: Long = DEF_ACTION_DEBOUNCE_INTERVAL
): ReadOnlyProperty<RvmPropertiesSupport, RvmAction<Unit>> = action(
    debounceInterval = debounceInterval
)
