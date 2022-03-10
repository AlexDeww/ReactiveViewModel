package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.property.State
import com.alexdeww.reactiveviewmodel.widget.BaseVisualControl
import com.alexdeww.reactiveviewmodel.widget.FormatterAction
import com.alexdeww.reactiveviewmodel.widget.InputControl
import com.alexdeww.reactiveviewmodel.widget.RatingControl
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.delegate(
    initValue: (thisRef: ReactiveViewModel, stateHandle: SavedStateHandle, key: String) -> T
): ReadWriteProperty<ReactiveViewModel, T> = SavedStateProperty(this, initValue)

fun <T : Any> SavedStateHandle.value(
    initialValue: T? = null
): ReadWriteProperty<ReactiveViewModel, T?> = delegate { _, stateHandle, key ->
    if (stateHandle.contains(key)) stateHandle[key]
    else initialValue
}

fun <T : Any> SavedStateHandle.valueNonNull(
    defaultValue: T
): ReadWriteProperty<ReactiveViewModel, T> = delegate { _, stateHandle, key ->
    stateHandle[key] ?: defaultValue
}

fun <T : Any> SavedStateHandle.state(
    initialValue: T? = null,
    debounceInterval: Long? = null
): ReadOnlyProperty<ReactiveViewModel, State<T>> = delegate { thisRef, stateHandle, key ->
    val state = State(stateHandle[key] ?: initialValue, debounceInterval)
    thisRef.run { state.viewFlowable.subscribe { stateHandle[key] = it }.disposeOnCleared() }
    state
}

fun SavedStateHandle.inputControl(
    initialText: String = "",
    hideErrorOnUserInput: Boolean = true,
    formatter: FormatterAction? = null,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<ReactiveViewModel, InputControl> = delegate { thisRef, stateHandle, key ->
    val textKey = "$key.text"
    val errorKey = "$key.error"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = InputControl(
        initialText = stateHandle[textKey] ?: initialText,
        hideErrorOnUserInput = hideErrorOnUserInput,
        formatter = formatter,
        initialEnabled = stateHandle[enabledKey] ?: initialEnabled,
        initialVisibility = stateHandle[visibilityKey] ?: initialVisibility
    )
    thisRef.run {
        control.value.viewFlowable
            .subscribe { stateHandle[textKey] = it }
            .disposeOnCleared()
        control.error.viewFlowable
            .subscribe { stateHandle[errorKey] = it }
            .disposeOnCleared()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .disposeOnCleared()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .disposeOnCleared()
    }
    control
}

fun SavedStateHandle.ratingControl(
    initialValue: Float = 0f,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<ReactiveViewModel, RatingControl> = delegate { thisRef, stateHandle, key ->
    val ratingKey = "$key.rating"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = RatingControl(
        initialValue = stateHandle[ratingKey] ?: initialValue,
        initialEnabled = stateHandle[enabledKey] ?: initialEnabled,
        initialVisibility = stateHandle[visibilityKey] ?: initialVisibility
    )
    thisRef.run {
        control.value.viewFlowable
            .subscribe { stateHandle[ratingKey] = it }
            .disposeOnCleared()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .disposeOnCleared()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .disposeOnCleared()
    }
    control
}

@PublishedApi
internal class SavedStateProperty<T>(
    private val savedStateHandle: SavedStateHandle,
    private val initValue: (thisRef: ReactiveViewModel, stateHandle: SavedStateHandle, key: String) -> T
) : ReadWriteProperty<ReactiveViewModel, T> {

    private object NoneValue

    private var value: Any? = NoneValue

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: ReactiveViewModel, property: KProperty<*>): T {
        if (value === NoneValue) {
            value = initValue(thisRef, savedStateHandle, getStateKey(thisRef, property))
        }
        return value as T
    }

    override fun setValue(thisRef: ReactiveViewModel, property: KProperty<*>, value: T) {
        this.value = value
        savedStateHandle[getStateKey(thisRef, property)] = value
    }

    private fun getStateKey(thisRef: ReactiveViewModel, property: KProperty<*>): String =
        "${thisRef::class.java.simpleName}.${property.name}"

}
