package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.core.Maybe

sealed class DialogResult {
    object Accept : DialogResult()
    object Cancel : DialogResult()
}

class DialogControl<T : Any, R : Any> internal constructor() : BaseControl() {

    sealed class Display<out T : Any> {
        data class Displayed<T : Any>(val data: T) : Display<T>()
        object Absent : Display<Nothing>()
    }

    internal val result = action<R>()

    val displayed = state<Display<T>>(Display.Absent)
    val isShowing get() = displayed.value is Display.Displayed

    fun show(data: T) {
        dismiss()
        displayed.consumer.accept(Display.Displayed(data))
    }

    fun showForResult(data: T, dismissOnDispose: Boolean = false): Maybe<R> {
        dismiss()
        return result
            .observable
            .doOnSubscribe { displayed.consumer.accept(Display.Displayed(data)) }
            .doOnDispose { if (dismissOnDispose) dismiss() }
            .takeUntil(
                displayed.observable
                    .skip(1)
                    .filter { it == Display.Absent }
            )
            .firstElement()
    }

    fun dismiss() {
        if (isShowing) displayed.consumer.accept(Display.Absent)
    }

}

class DialogControlResult<R : Any> internal constructor(
    private val dialogControl: DialogControl<*, R>
) {

    fun sendResult(result: R) {
        dialogControl.result.consumer.accept(result)
        dialogControl.dismiss()
    }

    fun sendResultWithoutDismiss(result: R) {
        dialogControl.result.consumer.accept(result)
    }

    fun dismiss() {
        dialogControl.dismiss()
    }

}

fun <T : Any, R : Any> dialogControl(): DialogControl<T, R> = DialogControl()

fun <T : Any> dialogControlWithResult(): DialogControl<T, DialogResult> = DialogControl()

typealias ActionCreateDialog<T, R> = (data: T, dc: DialogControlResult<R>) -> Dialog

fun <T : Any, R : Any> DialogControl<T, R>.bindTo(
    rvmViewComponent: RvmViewComponent,
    createDialog: ActionCreateDialog<T, R>
) {
    val liveData = DialogLiveDataMediator(
        control = this,
        createDialog = createDialog
    )
    rvmViewComponent.run { liveData.observe { /* empty */ } }
}

private class DialogLiveDataMediator<T : Any, R : Any>(
    control: DialogControl<T, R>,
    createDialog: ActionCreateDialog<T, R>
) : MediatorLiveData<DialogControl.Display<T>>() {

    private var dialog: Dialog? = null

    init {
        addSource(control.displayed.liveData) { displayData ->
            value = displayData
            when (displayData) {
                is DialogControl.Display.Displayed -> {
                    dialog = createDialog(displayData.data, DialogControlResult(control))
                    dialog?.setOnDismissListener { control.dismiss() }
                    dialog?.show()
                }
                DialogControl.Display.Absent -> closeDialog()
            }
        }
    }

    override fun removeObserver(observer: Observer<in DialogControl.Display<T>>) {
        super.removeObserver(observer)
        closeDialog()
    }

    private fun closeDialog() {
        dialog?.apply {
            setOnDismissListener(null)
            dismiss()
        }
        dialog = null
    }

}
