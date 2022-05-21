package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import androidx.lifecycle.LiveData
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

fun interface DialogFactory<in T : Any, R : Any, out D : Any> {
    fun createDialog(data: T, dc: DialogControlResult<R>): D
}

fun <T : Any, R : Any, D : Any> DialogControl<T, R>.bindToEx(
    rvmViewComponent: RvmViewComponent,
    dialogFactory: DialogFactory<T, R, D>,
    dialogLiveDataProvider: (control: DialogControl<T, R>, dialogFactory: DialogFactory<T, R, D>) -> DialogLiveDataMediator<T, R, D>
) {
    val liveData = dialogLiveDataProvider(this, dialogFactory)
    rvmViewComponent.run { liveData.observe { /* empty */ } }
}

fun <T : Any, R : Any> DialogControl<T, R>.bindTo(
    rvmViewComponent: RvmViewComponent,
    dialogFactory: DialogFactory<T, R, Dialog>
) = bindToEx(rvmViewComponent, dialogFactory, ::DialogLiveDataMediatorDialog)

abstract class DialogLiveDataMediator<T : Any, R : Any, D : Any>(
    control: DialogControl<T, R>,
    dialogFactory: DialogFactory<T, R, D>
) : MediatorLiveData<DialogControl.Display<T>>() {

    private var dialog: D? = null

    init {
        addSource(control.displayed.liveData) { displayData ->
            value = displayData
            when (displayData) {
                is DialogControl.Display.Displayed -> {
                    dialog = dialogFactory
                        .createDialog(
                            data = displayData.data,
                            dc = DialogControlResult(control)
                        ).also {
                            setupOnDismiss(it) { control.dismiss() }
                            showDialog(it)
                        }
                }
                DialogControl.Display.Absent -> closeDialog()
            }
        }
    }

    final override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        super.addSource(source, onChanged)
    }

    final override fun removeObserver(observer: Observer<in DialogControl.Display<T>>) {
        super.removeObserver(observer)
        closeDialog()
    }

    protected abstract fun setupOnDismiss(dialog: D, dismissAction: () -> Unit)

    protected abstract fun showDialog(dialog: D)

    protected abstract fun onCloseDialog(dialog: D)

    private fun closeDialog() {
        dialog?.let { onCloseDialog(it) }
        dialog = null
    }

}

private class DialogLiveDataMediatorDialog<T : Any, R : Any>(
    control: DialogControl<T, R>,
    dialogFactory: DialogFactory<T, R, Dialog>
) : DialogLiveDataMediator<T, R, Dialog>(control, dialogFactory) {

    override fun setupOnDismiss(dialog: Dialog, dismissAction: () -> Unit) {
        dialog.setOnDismissListener { dismissAction() }
    }

    override fun showDialog(dialog: Dialog) {
        dialog.show()
    }

    override fun onCloseDialog(dialog: Dialog) {
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

}
