package com.alexdeww.reactiveviewmodel.core.widget

import com.alexdeww.reactiveviewmodel.core.BaseControl
import io.reactivex.Maybe

sealed class DialogResult {
    object Accept : DialogResult()
    object Cancel : DialogResult()
}

class DialogControl<T, R> internal constructor() : BaseControl() {

    sealed class Display {
        data class Displayed<T>(val data: T) : Display()
        object Absent : Display()
    }

    private val result = action<R>()

    val displayed = state<Display>(Display.Absent)
    val isShowing
        get() = displayed.value is Display.Displayed<*>

    fun show(data: T) {
        dismiss()
        displayed.consumer.accept(Display.Displayed(data))
    }

    fun showForResult(data: T): Maybe<R> {
        dismiss()
        return result
            .observable
            .doOnSubscribe { displayed.consumer.accept(Display.Displayed(data)) }
            .takeUntil(
                displayed
                    .observable
                    .skip(1)
                    .filter { it == Display.Absent }
            )
            .firstElement()
    }

    fun sendResult(result: R) {
        this.result.consumer.accept(result)
        dismiss()
    }

    fun sendResultWithoutDismiss(result: R) {
        this.result.consumer.accept(result)
    }

    fun dismiss() {
        if (isShowing) displayed.consumer.accept(Display.Absent)
    }

}

fun <T, R> dialogControl(): DialogControl<T, R> = DialogControl()