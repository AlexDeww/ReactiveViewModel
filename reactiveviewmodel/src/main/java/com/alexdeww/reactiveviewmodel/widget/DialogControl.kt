package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.core.Maybe

sealed class DialogResult {
    object Accept : DialogResult()
    object Cancel : DialogResult()
}

class DialogControl<T, R> internal constructor() : BaseControl() {

    sealed class Display {
        data class Displayed<T>(val data: T) : Display()
        object Absent : Display()
    }

    internal val result = action<R>()

    val displayed = state<Display>(Display.Absent)
    val isShowing get() = displayed.value is Display.Displayed<*>

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

class DialogControlResult<R> internal constructor(
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

fun <T, R> dialogControl(): DialogControl<T, R> = DialogControl()

typealias ActionCreateDialog<T, R> = (data: T, dc: DialogControlResult<R>) -> Dialog

fun <T, R> DialogControl<T, R>.bindTo(
    rvmViewComponent: RvmViewComponent,
    createDialog: ActionCreateDialog<T, R>
) {
    val liveData = DialogLiveDataMediator(
        control = this,
        createDialog = createDialog
    )
    rvmViewComponent.run { liveData.observe { /* empty */ } }
}

private class DialogLiveDataMediator<T, R>(
    control: DialogControl<T, R>,
    createDialog: ActionCreateDialog<T, R>
) : MediatorLiveData<DialogControl.Display>() {

    private var dialog: Dialog? = null

    init {
        addSource(control.displayed.liveData) {
            value = it
            when {
                it is DialogControl.Display.Displayed<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    dialog = createDialog(it.data as T, DialogControlResult(control))
                    dialog?.setOnDismissListener { control.dismiss() }
                    dialog?.show()
                }
                it === DialogControl.Display.Absent -> closeDialog()
            }
        }
    }

    override fun removeObserver(observer: Observer<in DialogControl.Display>) {
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
