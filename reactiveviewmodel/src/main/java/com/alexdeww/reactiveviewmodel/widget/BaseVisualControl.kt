package com.alexdeww.reactiveviewmodel.widget

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.MediatorLiveData
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.alexdeww.reactiveviewmodel.core.action
import com.alexdeww.reactiveviewmodel.core.state
import io.reactivex.rxjava3.functions.Consumer
import java.lang.ref.WeakReference

typealias ActionOnValueChanged<T> = (newValue: T) -> Unit
typealias ActionOnActive<T> = VisualControlLiveDataMediator<T>.() -> Unit
typealias ActionOnInactive<T> = VisualControlLiveDataMediator<T>.() -> Unit

abstract class BaseVisualControl<T : Any, B : BaseVisualControl.BaseBinder<T, *>>(
    initialValue: T,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseControl<B>() {

    abstract class BaseBinder<T : Any, V : View>(
        rvmViewComponent: RvmViewComponent
    ) : ViewBinder(rvmViewComponent) {

        protected abstract val control: BaseVisualControl<T, *>

        @Suppress("LongParameterList")
        protected fun bindTo(
            view: V,
            bindEnable: Boolean,
            bindVisible: Boolean,
            onValueChanged: ActionOnValueChanged<T>,
            onActiveAction: ActionOnActive<T>,
            onInactiveAction: ActionOnInactive<T>
        ) {
            val liveData = VisualControlLiveDataMediator(
                control = control,
                view = view,
                bindEnable = bindEnable,
                bindVisible = bindVisible,
                onValueChanged = onValueChanged,
                onActiveAction = onActiveAction,
                onInactiveAction = onInactiveAction
            )
            rvmViewComponentRef.get()?.run { liveData.observe { /* empty */ } }
        }

    }

    enum class Visibility(val value: Int) {
        VISIBLE(View.VISIBLE),
        INVISIBLE(View.INVISIBLE),
        GONE(View.GONE)
    }

    val value by state(initialValue)
    val enabled by state(initialEnabled)
    val visibility by state(initialVisibility)

    val actionChangeValue by action<T>()

    init {
        actionChangeValue.observable
            .filter { it != value.value }
            .subscribe(::onChangedValue)
    }

    @CallSuper
    protected open fun onChangedValue(newValue: T) {
        value.consumer.accept(newValue)
    }

}

@Suppress("LongParameterList")
class VisualControlLiveDataMediator<T : Any> internal constructor(
    control: BaseVisualControl<T, *>,
    view: View,
    private val bindEnable: Boolean,
    private val bindVisible: Boolean,
    private val onValueChanged: ActionOnValueChanged<T>,
    private val onActiveAction: ActionOnActive<T>,
    private val onInactiveAction: ActionOnInactive<T>
) : MediatorLiveData<Unit>() {

    private val viewRef = WeakReference(view)
    private val view: View? get() = viewRef.get()
    private val controlRef = WeakReference(control)
    private val control: BaseVisualControl<T, *>? get() = controlRef.get()
    private var isEditing: Boolean = false

    val changeValueConsumer = Consumer<T> {
        if (!isEditing) this.control?.actionChangeValue?.call(it)
    }

    override fun onActive() {
        super.onActive()
        control?.apply {
            if (bindEnable) addSource(enabled.liveData) { view?.isEnabled = it }
            if (bindVisible) addSource(visibility.liveData) { view?.visibility = it.value }
            addSource(value.liveData) { newValue ->
                isEditing = true
                onValueChanged(newValue)
                isEditing = false
            }
        }
        onActiveAction.invoke(this)
    }

    override fun onInactive() {
        control?.apply {
            if (bindEnable) removeSource(enabled.liveData)
            if (bindVisible) removeSource(visibility.liveData)
            removeSource(value.liveData)
        }
        onInactiveAction.invoke(this)
        super.onInactive()
    }

}
