package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.component.ReactiveViewModel
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import com.alexdeww.reactiveviewmodel.widget.*
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
): ReadOnlyProperty<ReactiveViewModel, RvmState<T>> = delegate { thisRef, stateHandle, key ->
    val state = RvmState(stateHandle[key] ?: initialValue, debounceInterval)
    thisRef.run { state.viewFlowable.subscribe { stateHandle[key] = it }.autoDispose() }
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
            .autoDispose()
        control.error.viewFlowable
            .subscribe { stateHandle[errorKey] = it }
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
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
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
    }
    control
}

fun <T : Any> SavedStateHandle.displayableControl(
    debounceInterval: Long? = null
): ReadOnlyProperty<ReactiveViewModel, DisplayableControl<T>> =
    delegate { thisRef, stateHandle, key ->
        val actionKey = "$key.action"
        val control = DisplayableControl<T>(debounceInterval)
        thisRef.run {
            control.action.setValue(stateHandle[actionKey] ?: DisplayableControl.Action.Hide)
            control.action.viewFlowable
                .subscribe { stateHandle[actionKey] = it }
                .autoDispose()
        }
        control
    }

fun SavedStateHandle.checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<ReactiveViewModel, CheckControl> = delegate { thisRef, stateHandle, key ->
    val checkedKey = "$key.checked"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = CheckControl(
        initialChecked = stateHandle[checkedKey] ?: initialChecked,
        initialEnabled = stateHandle[enabledKey] ?: initialEnabled,
        initialVisibility = stateHandle[visibilityKey] ?: initialVisibility
    )
    thisRef.run {
        control.value.viewFlowable
            .subscribe { stateHandle[checkedKey] = it }
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
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
