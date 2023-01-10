package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import androidx.lifecycle.MediatorLiveData
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyDelegate
import io.reactivex.rxjava3.core.Maybe
import kotlin.properties.ReadOnlyProperty

typealias RvmDialogCreator<T, R, D> = (data: T, dc: RvmDialogControlResult<R>) -> D

sealed class RvmDefaultDialogResult {
    object Accept : RvmDefaultDialogResult()
    object Cancel : RvmDefaultDialogResult()
}

class RvmDialogControl<T : Any, R : Any> internal constructor() :
    RvmBaseControl<RvmDialogControl<T, R>.Binder>() {

    sealed class Display<out T : Any> {
        data class Displayed<T : Any>(val data: T) : Display<T>()
        object Absent : Display<Nothing>()
    }

    internal val result by RVM.action<R>()
    internal val displayedInternal by RVM.state<Display<T>>(Display.Absent)

    val displayed by RVM.stateProjection(displayedInternal, false) { it }
    val isShowing get() = displayed.value is Display.Displayed

    fun show(data: T) {
        dismiss()
        displayedInternal.consumer.accept(Display.Displayed(data))
    }

    fun showForResult(data: T, dismissOnDispose: Boolean = false): Maybe<R> {
        dismiss()
        return result
            .observable
            .doOnSubscribe { displayedInternal.consumer.accept(Display.Displayed(data)) }
            .doOnDispose { if (dismissOnDispose) dismiss() }
            .takeUntil(
                displayedInternal.observable
                    .skip(1)
                    .filter { it == Display.Absent }
            )
            .firstElement()
    }

    fun dismiss() {
        if (isShowing) displayedInternal.consumer.accept(Display.Absent)
    }

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : ViewBinder(rvmViewComponent) {

        @RvmBinderDslMarker
        fun <D : Any> bindTo(
            dialogHandlerListener: RvmDialogHandlerListener<D>,
            dialogCreator: RvmDialogCreator<T, R, D>,
        ) {
            val liveData = RvmDialogLiveDataMediator(
                control = this@RvmDialogControl,
                dialogCreator = dialogCreator,
                dialogHandlerListener = dialogHandlerListener
            )
            rvmViewComponentRef.get()?.run { liveData.observe { /* empty */ } }
        }

        @RvmBinderDslMarker
        fun bindTo(dialogCreator: RvmDialogCreator<T, R, Dialog>) = bindTo(
            dialogHandlerListener = RvmOrdinaryDialogHandlerListener(),
            dialogCreator = dialogCreator
        )

    }

}

class RvmDialogControlResult<R : Any> internal constructor(
    private val dialogControl: RvmDialogControl<*, R>
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

@Suppress("unused")
@RvmDslMarker
fun <T : Any, R : Any> RVM.dialogControl(): ReadOnlyProperty<
        RvmPropertiesSupport,
        RvmDialogControl<T, R>> = RvmPropertyDelegate.def { RvmDialogControl() }

@Suppress("unused")
@RvmDslMarker
fun <T : Any> RVM.dialogControlDefaultResult(): ReadOnlyProperty<
        RvmPropertiesSupport,
        RvmDialogControl<T, RvmDefaultDialogResult>> = dialogControl()

interface RvmDialogHandlerListener<D> {

    fun onSetupOnDismiss(dialog: D, dismissAction: () -> Unit)

    fun onShowDialog(dialog: D)

    fun onCloseDialog(dialog: D)

    fun onDialogUnbind(dialog: D) {
        onCloseDialog(dialog)
    }

}

private class RvmDialogLiveDataMediator<T : Any, R : Any, D : Any>(
    private val control: RvmDialogControl<T, R>,
    private val dialogCreator: RvmDialogCreator<T, R, D>,
    private val dialogHandlerListener: RvmDialogHandlerListener<D>
) : MediatorLiveData<RvmDialogControl.Display<T>>(),
    RvmDialogHandlerListener<D> by dialogHandlerListener {

    private var dialog: D? = null

    override fun onActive() {
        super.onActive()
        addSource(control.displayedInternal.liveData) { displayData ->
            value = displayData
            when (displayData) {
                is RvmDialogControl.Display.Displayed -> {
                    dialog = dialogCreator(displayData.data, RvmDialogControlResult(control)).also {
                        onSetupOnDismiss(it) { control.dismiss() }
                        onShowDialog(it)
                    }
                }
                RvmDialogControl.Display.Absent -> {
                    dialog?.let { onCloseDialog(it) }
                    releaseDialog()
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        removeSource(control.displayedInternal.liveData)
        dialog?.let { onDialogUnbind(it) }
        releaseDialog()
    }

    private fun releaseDialog() {
        dialog = null
    }

}

private class RvmOrdinaryDialogHandlerListener : RvmDialogHandlerListener<Dialog> {

    override fun onSetupOnDismiss(dialog: Dialog, dismissAction: () -> Unit) {
        dialog.setOnDismissListener { dismissAction() }
    }

    override fun onShowDialog(dialog: Dialog) {
        dialog.show()
    }

    override fun onCloseDialog(dialog: Dialog) {
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

}
