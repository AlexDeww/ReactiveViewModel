package com.alexdeww.reactiveviewmodel.widget

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable

class NotificationControl<T> internal constructor() : BaseControl() {

    sealed class Action {
        data class Show<T>(val data: T) : Action()
        object Hide : Action()
    }

    val action = state<Action>(Action.Hide)
    val isShowing get() = action.value.isShowing
    val showingValue: T? = action.value.getShowingValue()

    fun show(data: T) {
        action.consumer.accept(Action.Show(data))
    }

    fun hide() {
        action.consumer.accept(Action.Hide)
    }

    internal val Action?.isShowing: Boolean get() = this is Action.Show<*>
    @Suppress("UNCHECKED_CAST")
    internal fun <T> Action?.getShowingValue(): T? = (this as? Action.Show<T>)?.data

}

fun <T> notificationControl(): NotificationControl<T> = NotificationControl()

typealias NotificationAction<T> = (isVisible: Boolean, data: T?) -> Unit

fun <T> NotificationControl<T>.bindTo(
    action: NotificationAction<T>
): Disposable = this.action
    .observable
    .toFlowable(BackpressureStrategy.LATEST)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { action.invoke(it.isShowing, it.getShowingValue<T>()) }
