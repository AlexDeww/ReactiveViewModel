package com.alexdeww.reactiveviewmodel.widget

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable

class DisplayableControl<T> internal constructor() : BaseControl() {

    sealed class Action<out T> {
        object Hide : Action<Nothing>()
        data class Show<T>(val data: T) : Action<T>()

        val isShowing: Boolean get() = this is Show<*>
        fun getShowingValue(): T? = (this as? Show<T>)?.data
    }

    val action = state<Action<T>>(Action.Hide)
    val isShowing get() = action.value?.isShowing ?: false
    val showingValue: T? get() = action.value?.getShowingValue()

    fun show(data: T) {
        action.consumer.accept(Action.Show(data))
    }

    fun hide() {
        action.consumer.accept(Action.Hide)
    }

}

fun <T> displayableControl(): DisplayableControl<T> = DisplayableControl()

typealias DisplayableAction<T> = (isVisible: Boolean, data: T?) -> Unit

fun <T> DisplayableControl<T>.bindTo(
    action: DisplayableAction<T>
): Disposable = this.action
    .observable
    .toFlowable(BackpressureStrategy.LATEST)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { action.invoke(it.isShowing, it.getShowingValue()) }
