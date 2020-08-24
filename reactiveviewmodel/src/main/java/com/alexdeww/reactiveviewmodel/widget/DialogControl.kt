package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
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

    internal val result = action<R>()

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

class DialogControlResult<T, R> internal constructor(
    private val dialogControl: DialogControl<T, R>
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

typealias ActionCreateDialog<T, R> = (data: T, dc: DialogControlResult<T, R>) -> Dialog

fun <T, R> DialogControl<T, R>.bindTo(
    rvmViewComponent: RvmViewComponent,
    createDialog: ActionCreateDialog<T, R>
) {
    val mediator = object : MediatorLiveData<DialogControl.Display>() {
        private var dialog: Dialog? = null

        init {
            addSource(displayed.liveData) {
                value = it
                when {
                    it is DialogControl.Display.Displayed<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        dialog = createDialog(it.data as T, DialogControlResult(this@bindTo))
                        dialog?.setOnDismissListener { this@bindTo.dismiss() }
                        dialog?.show()
                    }
                    it === DialogControl.Display.Absent -> closeDialog()
                }
            }
        }

        override fun onInactive() {
            closeDialog()
            super.onInactive()
        }

        private fun closeDialog() {
            dialog?.apply {
                setOnDismissListener(null)
                dismiss()
            }
            dialog = null
        }
    }
    rvmViewComponent.run { mediator.observe { /* empty */ } }
}
