package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable

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
    val isShowing get() = displayed.value is Display.Displayed<*>

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

fun <T, R> DialogControl<T, R>.bindTo(
    createDialog: (data: T, dc: DialogControl<T, R>) -> Dialog
): Disposable {
    var dialog: Dialog? = null
    val closeDialog: () -> Unit = {
        dialog?.setOnDismissListener(null)
        dialog?.dismiss()
        dialog = null
    }

    return displayed
        .observable
        .toFlowable(BackpressureStrategy.LATEST)
        .observeOn(AndroidSchedulers.mainThread())
        .doFinally { closeDialog() }
        .subscribe {
            when {
                it is DialogControl.Display.Displayed<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    dialog = createDialog(it.data as T, this)
                    dialog?.setOnDismissListener { this.dismiss() }
                    dialog?.show()
                }
                it === DialogControl.Display.Absent -> closeDialog()
            }
        }
}