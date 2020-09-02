package com.alexdeww.reactiveviewmodel.widget

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.MediatorLiveData
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.lang.ref.WeakReference

abstract class BaseVisualControl<T>(
    initialValue: T,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseControl() {

    enum class Visibility(val value: Int) {
        VISIBLE(View.VISIBLE),
        INVISIBLE(View.INVISIBLE),
        GONE(View.GONE)
    }

    val value = state(initialValue)
    val enabled = state(initialEnabled)
    val visibility = state(initialVisibility)

    val actionChangeValue = action<T>()

    init {
        actionChangeValue.observable
            .filter { it != value.value }
            .let { transformObservable(it) }
            .subscribe(::onChangedValue)
    }

    protected open fun transformObservable(observable: Observable<T>): Observable<T> = observable

    @CallSuper
    protected open fun onChangedValue(newValue: T) {
        value.consumer.accept(newValue)
    }

}

typealias ActionOnValueChanged<T> = (newValue: T) -> Unit
typealias ActionOnActive<T> = VisualControlLiveDataMediator<T>.() -> Unit
typealias ActionOnInactive<T> = VisualControlLiveDataMediator<T>.() -> Unit

fun <C : BaseVisualControl<T>, T, V : View> C.baseBindTo(
    rvmViewComponent: RvmViewComponent,
    view: V,
    bindEnable: Boolean,
    bindVisible: Boolean,
    onValueChanged: ActionOnValueChanged<T>,
    onActiveAction: ActionOnActive<T>,
    onInactiveAction: ActionOnInactive<T>
) {
    val liveData = VisualControlLiveDataMediator(
        control = this@baseBindTo,
        view = view,
        bindEnable = bindEnable,
        bindVisible = bindVisible,
        onValueChanged = onValueChanged,
        onActiveAction = onActiveAction,
        onInactiveAction = onInactiveAction
    )
    rvmViewComponent.run { liveData.observe { /* empty */ } }
}

class VisualControlLiveDataMediator<T>(
    control: BaseVisualControl<T>,
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
    private val control: BaseVisualControl<T>? get() = controlRef.get()
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
