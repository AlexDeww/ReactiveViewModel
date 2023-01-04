package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.delegate(
    initValue: (thisRef: RvmViewModelComponent, stateHandle: SavedStateHandle, key: String) -> T
): ReadWriteProperty<RvmViewModelComponent, T> = SavedStateProperty(this, initValue)

fun <T : Any> SavedStateHandle.value(
    initialValue: T? = null
): ReadWriteProperty<RvmViewModelComponent, T?> = delegate { _, stateHandle, key ->
    if (stateHandle.contains(key)) stateHandle[key]
    else initialValue
}

fun <T : Any> SavedStateHandle.valueNonNull(
    defaultValue: T
): ReadWriteProperty<RvmViewModelComponent, T> = delegate { _, stateHandle, key ->
    stateHandle[key] ?: defaultValue
}

fun <T : Any> SavedStateHandle.state(
    initialValue: T? = null,
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmViewModelComponent, RvmState<T>> = delegate { thisRef, stateHandle, key ->
    val state = RvmState(stateHandle[key] ?: initialValue, debounceInterval)
    thisRef.run { state.viewFlowable.subscribe { stateHandle[key] = it }.autoDispose() }
    state
}

@PublishedApi
internal class SavedStateProperty<T>(
    private val savedStateHandle: SavedStateHandle,
    private val initValue: (thisRef: RvmViewModelComponent, stateHandle: SavedStateHandle, key: String) -> T
) : ReadWriteProperty<RvmViewModelComponent, T> {

    private object NoneValue

    private var value: Any? = NoneValue

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: RvmViewModelComponent, property: KProperty<*>): T {
        if (value === NoneValue) {
            value = initValue(thisRef, savedStateHandle, getStateKey(thisRef, property))
        }
        return value as T
    }

    override fun setValue(thisRef: RvmViewModelComponent, property: KProperty<*>, value: T) {
        this.value = value
        savedStateHandle[getStateKey(thisRef, property)] = value
    }

    private fun getStateKey(thisRef: RvmViewModelComponent, property: KProperty<*>): String =
        "${thisRef::class.java.simpleName}.${property.name}"

}
