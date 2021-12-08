package com.alexdeww.reactiveviewmodel.core

import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RatingBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import com.alexdeww.reactiveviewmodel.widget.*
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.disposables.Disposable

interface RvmViewComponent {

    val componentLifecycleOwner: LifecycleOwner

    fun Disposable.disposeOnDestroy(tag: String)

    fun Disposable.disposeOnStop(tag: String)

    fun Disposable.disposeOnDestroyView(tag: String)

    fun <T> LiveData<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(owner = componentLifecycleOwner, action = action)

    fun <T : Any> State<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T : Any> Event<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T : Any> ConfirmationEvent<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T : Any> DisplayableControl<T>.observe(
        action: DisplayableAction<T>
    ): Observer<DisplayableControl.Action<T>> = this@observe.action.observe {
        action.invoke(it.isShowing, it.getShowingValue())
    }

    fun <T : Any> DisplayableControl<T>.observe(
        onShow: (T) -> Unit,
        onHide: () -> Unit
    ): Observer<DisplayableControl.Action<T>> = this@observe.action.observe {
        when (it) {
            is DisplayableControl.Action.Show<T> -> onShow.invoke(it.data)
            else -> onHide.invoke()
        }
    }

    fun CheckControl.bindTo(
        compoundButton: CompoundButton,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = bindTo(
        rvmViewComponent = this@RvmViewComponent,
        compoundButton = compoundButton,
        bindEnable = bindEnable,
        bindVisible = bindVisible
    )

    fun <T : Any, R : Any> DialogControl<T, R>.bindTo(
        createDialog: ActionCreateDialog<T, R>
    ) = bindTo(
        rvmViewComponent = this@RvmViewComponent,
        createDialog = createDialog
    )

    fun InputControl.bindTo(
        editText: EditText,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = bindTo(
        rvmViewComponent = this@RvmViewComponent,
        editText = editText,
        bindError = bindError,
        bindEnable = bindEnable,
        bindVisible = bindVisible
    )

    fun InputControl.bindTo(
        textInputLayout: TextInputLayout,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = bindTo(
        rvmViewComponent = this@RvmViewComponent,
        textInputLayout = textInputLayout,
        bindError = bindError,
        bindEnable = bindEnable,
        bindVisible = bindVisible
    )

    fun RatingControl.bindTo(
        ratingBar: RatingBar,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = bindTo(
        rvmViewComponent = this@RvmViewComponent,
        ratingBar = ratingBar,
        bindEnable = bindEnable,
        bindVisible = bindVisible
    )

}
