package com.alexdeww.reactiveviewmodel.widget

import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent

class DisplayableControl<T> internal constructor(debounceInterval: Long? = null) : BaseControl() {

    sealed class Action<out T> {
        object Hide : Action<Nothing>()
        data class Show<T>(val data: T) : Action<T>()

        val isShowing: Boolean get() = this is Show<*>
        fun getShowingValue(): T? = (this as? Show<T>)?.data
    }

    val action = state<Action<T>>(Action.Hide, debounceInterval)
    val isShowing get() = action.value?.isShowing ?: false
    val showingValue: T? get() = action.value?.getShowingValue()

    fun show(data: T) {
        action.consumer.accept(Action.Show(data))
    }

    fun hide() {
        action.consumer.accept(Action.Hide)
    }

}

fun <T> displayableControl(debounceInterval: Long? = null): DisplayableControl<T> =
    DisplayableControl(debounceInterval)

typealias DisplayableAction<T> = (isVisible: Boolean, data: T?) -> Unit

fun <T> DisplayableControl<T>.observe(
    rvmViewComponent: RvmViewComponent,
    action: DisplayableAction<T>
): Observer<DisplayableControl.Action<T>> = rvmViewComponent.run {
    this@observe.action.observe { action.invoke(it.isShowing, it.getShowingValue()) }
}

fun <T> DisplayableControl<T>.observe(
    rvmViewComponent: RvmViewComponent,
    onShow: (T) -> Unit,
    onHide: () -> Unit
): Observer<DisplayableControl.Action<T>> = rvmViewComponent.run {
    this@observe.action.observe {
        when (it) {
            is DisplayableControl.Action.Show<T> -> onShow.invoke(it.data)
            else -> onHide.invoke()
        }
    }
}
