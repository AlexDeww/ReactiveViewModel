package com.alexdeww.reactiveviewmodel.widget

import android.os.Parcelable
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

class DisplayableControl<T : Any> internal constructor(
    debounceInterval: Long? = null
) : BaseControl() {

    sealed class Action<out T : Any> : Parcelable {

        @Parcelize
        object Hide : Action<Nothing>()

        @Parcelize
        data class Show<T : Any>(val data: @RawValue T) : Action<T>()

        val isShowing: Boolean get() = this is Show
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

fun <T : Any> displayableControl(debounceInterval: Long? = null): DisplayableControl<T> =
    DisplayableControl(debounceInterval)

typealias DisplayableAction<T> = (isVisible: Boolean, data: T?) -> Unit

fun <T : Any> DisplayableControl<T>.observe(
    rvmViewComponent: RvmViewComponent,
    action: DisplayableAction<T>
): Observer<DisplayableControl.Action<T>> = rvmViewComponent.run {
    this@observe.action.observe { action.invoke(it.isShowing, it.getShowingValue()) }
}

fun <T : Any> DisplayableControl<T>.observe(
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
