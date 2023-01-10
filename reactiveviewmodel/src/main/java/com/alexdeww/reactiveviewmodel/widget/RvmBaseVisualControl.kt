package com.alexdeww.reactiveviewmodel.widget

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.widget.RvmBaseVisualControl.Visibility
import io.reactivex.rxjava3.functions.Consumer
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty

typealias RvmActionOnValueChanged<T> = (newValue: T) -> Unit
typealias RvmActionOnActive<T> = RvmVisualControlLiveDataMediator<T>.() -> Unit
typealias RvmActionOnInactive<T> = RvmVisualControlLiveDataMediator<T>.() -> Unit

abstract class RvmBaseVisualControl<T : Any, B : RvmBaseVisualControl.BaseBinder<T, *>>(
    initialValue: T,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : RvmBaseControl<B>() {

    abstract class BaseBinder<T : Any, V : View>(
        rvmViewComponent: RvmViewComponent
    ) : ViewBinder(rvmViewComponent) {

        protected abstract val control: RvmBaseVisualControl<T, *>

        @Suppress("LongParameterList")
        protected fun bindTo(
            view: V,
            bindEnable: Boolean,
            bindVisible: Boolean,
            onValueChanged: RvmActionOnValueChanged<T>,
            onActiveAction: RvmActionOnActive<T>,
            onInactiveAction: RvmActionOnInactive<T>
        ) {
            val liveData = RvmVisualControlLiveDataMediator(
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

    protected val dataInternal by RVM.state(initialValue)
    internal val dataInternalAccess = dataInternal

    val data by RVM.stateProjection(dataInternal, false) { it }
    val enabled by RVM.state(initialEnabled)
    val visibility by RVM.state(initialVisibility)

    val actionChangeDataValue by RVM.action<T>()

    init {
        actionChangeDataValue.observable
            .filter { it != dataInternal.value }
            .subscribe(::onDataValueChanged)
    }

    @CallSuper
    protected open fun onDataValueChanged(newValue: T) {
        dataInternal.consumer.accept(newValue)
    }

}

@Suppress("LongParameterList")
class RvmVisualControlLiveDataMediator<T : Any> internal constructor(
    control: RvmBaseVisualControl<T, *>,
    view: View,
    private val bindEnable: Boolean,
    private val bindVisible: Boolean,
    private val onValueChanged: RvmActionOnValueChanged<T>,
    private val onActiveAction: RvmActionOnActive<T>,
    private val onInactiveAction: RvmActionOnInactive<T>
) : MediatorLiveData<Unit>() {

    private val viewRef = WeakReference(view)
    private val view: View? get() = viewRef.get()
    private val controlRef = WeakReference(control)
    private val control: RvmBaseVisualControl<T, *>? get() = controlRef.get()
    private var isEditing: Boolean = false

    val changeValueConsumer = Consumer<T> {
        if (!isEditing) this.control?.actionChangeDataValue?.call(it)
    }

    override fun onActive() {
        super.onActive()
        control?.apply {
            if (bindEnable) addSource(enabled.liveData) { view?.isEnabled = it }
            if (bindVisible) addSource(visibility.liveData) { view?.visibility = it.value }
            addSource(dataInternalAccess.liveData) { newValue ->
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
            removeSource(dataInternalAccess.liveData)
        }
        onInactiveAction.invoke(this)
        super.onInactive()
    }

}

typealias RvmInitControl<T, C> = (
    value: T,
    isEnabled: Boolean,
    visibility: Visibility,
    stateHandle: SavedStateHandle,
    key: String,
) -> C

fun <T : Any, C : RvmBaseVisualControl<T, *>> SavedStateHandle.visualControlDelegate(
    initialValue: T,
    initialEnabled: Boolean,
    initialVisibility: Visibility,
    initControl: RvmInitControl<T, C>,
    watcher: RvmViewModelComponent.(stateHandle: SavedStateHandle, key: String) -> Unit = { _, _ -> }
): ReadOnlyProperty<RvmViewModelComponent, C> = delegate { thisRef, stateHandle, key ->
    val dataKey = "$key.data"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = initControl(
        stateHandle[dataKey] ?: initialValue,
        stateHandle[enabledKey] ?: initialEnabled,
        stateHandle[visibilityKey] ?: initialVisibility,
        stateHandle, key
    )
    thisRef.run {
        control.data.viewFlowable
            .subscribe { stateHandle[dataKey] = it }
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
        watcher(stateHandle, key)
    }
    control
}

typealias RvmControlDefaultConstructor<T, C> = (value: T, isEnabled: Boolean, visibility: Visibility) -> C

inline fun <T : Any, C : RvmBaseVisualControl<T, *>> rvmControlDefaultConstructor(
    crossinline defaultConstructor: RvmControlDefaultConstructor<T, C>
): RvmInitControl<T, C> = { value: T, isEnabled: Boolean, visibility: Visibility, _, _ ->
    defaultConstructor(value, isEnabled, visibility)
}
