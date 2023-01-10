package com.alexdeww.reactiveviewmodel.widget

import android.app.Dialog
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RatingBar
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.google.android.material.textfield.TextInputLayout

@RvmBinderDslMarker
interface RvmWidgetBindShortcut : RvmViewComponent {

    @RvmBinderDslMarker
    fun RvmCheckControl.bindTo(
        compoundButton: CompoundButton,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(compoundButton, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun <T : Any, R : Any, D : Any> RvmDialogControl<T, R>.bindTo(
        dialogHandlerListener: RvmDialogHandlerListener<D>,
        dialogCreator: RvmDialogCreator<T, R, D>,
    ) = binder.bindTo(dialogHandlerListener, dialogCreator)

    @RvmBinderDslMarker
    fun <T : Any, R : Any> RvmDialogControl<T, R>.bindTo(
        dialogCreator: RvmDialogCreator<T, R, Dialog>
    ) = binder.bindTo(dialogCreator)

    @RvmBinderDslMarker
    fun <T : Any> RvmDisplayableControl<T>.bind(
        action: RvmDisplayableAction<T>
    ) = binder.bind(action)

    @RvmBinderDslMarker
    fun <T : Any> RvmDisplayableControl<T>.bind(
        onShow: (T) -> Unit,
        onHide: () -> Unit
    ) = binder.bind(onShow, onHide)

    @RvmBinderDslMarker
    fun RvmInputControl.bindTo(
        editText: EditText,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(editText, bindError, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun RvmInputControl.bindTo(
        textInputLayout: TextInputLayout,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(textInputLayout, bindError, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun RvmRatingControl.bindTo(
        ratingBar: RatingBar,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(ratingBar, bindEnable, bindVisible)

}
