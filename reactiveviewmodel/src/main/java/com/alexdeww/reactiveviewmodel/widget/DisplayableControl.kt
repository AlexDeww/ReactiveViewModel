package com.alexdeww.reactiveviewmodel.widget

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyReadOnlyDelegate
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlin.properties.ReadOnlyProperty

typealias DisplayableAction<T> = (isVisible: Boolean, data: T?) -> Unit

class DisplayableControl<T : Any> internal constructor(
    debounceInterval: Long? = null
) : BaseControl<DisplayableControl<T>.Binder>() {

    sealed class Action<out T : Any> : Parcelable {

        @Parcelize
        object Hide : Action<Nothing>()

        @Parcelize
        data class Show<T : Any>(val data: @RawValue T) : Action<T>()

        val isShowing: Boolean get() = this is Show
        fun getShowingValue(): T? = (this as? Show<T>)?.data

    }

    val action by state<Action<T>>(Action.Hide, debounceInterval)
    val isShowing get() = action.value?.isShowing ?: false
    val showingValue: T? get() = action.value?.getShowingValue()

    fun show(data: T) {
        action.consumer.accept(Action.Show(data))
    }

    fun hide() {
        action.consumer.accept(Action.Hide)
    }

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : ViewBinder(rvmViewComponent) {

        @RvmBinderDslMarker
        fun bind(action: DisplayableAction<T>) {
            rvmViewComponentRef.get()?.run {
                this@DisplayableControl.action.observe {
                    action.invoke(it.isShowing, it.getShowingValue())
                }
            }
        }

        @RvmBinderDslMarker
        fun bind(
            onShow: (T) -> Unit,
            onHide: () -> Unit
        ) {
            rvmViewComponentRef.get()?.run {
                this@DisplayableControl.action.observe {
                    when (it) {
                        is Action.Show<T> -> onShow.invoke(it.data)
                        else -> onHide.invoke()
                    }
                }
            }
        }

    }

}

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RvmWidgetsSupport.displayableControl(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmWidgetsSupport, DisplayableControl<T>> = RvmPropertyReadOnlyDelegate(
    property = DisplayableControl(debounceInterval)
)

@RvmDslMarker
fun <T : Any> SavedStateHandle.displayableControl(
    debounceInterval: Long? = null
): ReadOnlyProperty<RvmViewModelComponent, DisplayableControl<T>> = delegate { thisRef, sh, key ->
    val actionKey = "$key.action"
    val control = DisplayableControl<T>(debounceInterval)
    thisRef.run {
        control.action.setValue(sh[actionKey] ?: DisplayableControl.Action.Hide)
        control.action.viewFlowable
            .subscribe { sh[actionKey] = it }
            .autoDispose()
    }
    control
}
