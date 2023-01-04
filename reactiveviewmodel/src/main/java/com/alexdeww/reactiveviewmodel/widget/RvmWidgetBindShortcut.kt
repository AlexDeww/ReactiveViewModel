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
    fun CheckControl.bindTo(
        compoundButton: CompoundButton,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(compoundButton, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun <T : Any, R : Any, D : Any> DialogControl<T, R>.bindTo(
        dialogHandlerListener: DialogHandlerListener<D>,
        dialogCreator: DialogCreator<T, R, D>,
    ) = binder.bindTo(dialogHandlerListener, dialogCreator)

    @RvmBinderDslMarker
    fun <T : Any, R : Any> DialogControl<T, R>.bindTo(
        dialogCreator: DialogCreator<T, R, Dialog>
    ) = binder.bindTo(dialogCreator)

    @RvmBinderDslMarker
    fun <T : Any> DisplayableControl<T>.bind(
        action: DisplayableAction<T>
    ) = binder.bind(action)

    @RvmBinderDslMarker
    fun <T : Any> DisplayableControl<T>.bind(
        onShow: (T) -> Unit,
        onHide: () -> Unit
    ) = binder.bind(onShow, onHide)

    @RvmBinderDslMarker
    fun InputControl.bindTo(
        editText: EditText,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(editText, bindError, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun InputControl.bindTo(
        textInputLayout: TextInputLayout,
        bindError: Boolean = false,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(textInputLayout, bindError, bindEnable, bindVisible)

    @RvmBinderDslMarker
    fun RatingControl.bindTo(
        ratingBar: RatingBar,
        bindEnable: Boolean = true,
        bindVisible: Boolean = true
    ) = binder.bindTo(ratingBar, bindEnable, bindVisible)

}
