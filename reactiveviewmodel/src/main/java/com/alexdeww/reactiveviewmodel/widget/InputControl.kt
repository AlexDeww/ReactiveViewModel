package com.alexdeww.reactiveviewmodel.widget

import android.text.*
import android.view.View
import android.widget.EditText
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyReadOnlyDelegate
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.ReadOnlyProperty

typealias FormatterAction = (text: String) -> String

class InputControl internal constructor(
    initialText: String,
    private val hideErrorOnUserInput: Boolean,
    formatter: FormatterAction?,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<String, InputControl.Binder>(
    initialValue = initialText,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
) {

    init {
        dataInternal.valueChangesHook = formatter
    }

    val error by RVM.state<String>()

    override fun onDataValueChanged(newValue: String) {
        if (hideErrorOnUserInput) error.consumer.accept("")
        super.onDataValueChanged(newValue)
    }

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : BaseBinder<String, View>(rvmViewComponent) {

        override val control: BaseVisualControl<String, *> get() = this@InputControl

        @RvmBinderDslMarker
        fun bindTo(
            editText: EditText,
            bindError: Boolean = false,
            bindEnable: Boolean = true,
            bindVisible: Boolean = true
        ) = bindTo(
            view = editText,
            editText = editText,
            actionOnError = { editText.error = it },
            bindError = bindError,
            bindEnable = bindEnable,
            bindVisible = bindVisible
        )

        @RvmBinderDslMarker
        fun bindTo(
            textInputLayout: TextInputLayout,
            bindError: Boolean = false,
            bindEnable: Boolean = true,
            bindVisible: Boolean = true
        ) = bindTo(
            view = textInputLayout,
            editText = textInputLayout.editText!!,
            actionOnError = { textInputLayout.error = it },
            bindError = bindError,
            bindEnable = bindEnable,
            bindVisible = bindVisible
        )

        @Suppress("LongParameterList")
        private fun InputControl.bindTo(
            view: View,
            editText: EditText,
            actionOnError: (String) -> Unit,
            bindError: Boolean = false,
            bindEnable: Boolean = true,
            bindVisible: Boolean = true
        ) {
            var textWatcher: TextWatcher? = null
            bindTo(
                view = view,
                bindEnable = bindEnable,
                bindVisible = bindVisible,
                onValueChanged = { newValue ->
                    val editable = editText.text
                    if (editable != null && !newValue.contentEquals(editable)) {
                        val ss = SpannableString(newValue)
                        TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                        editable.replace(0, editable.length, ss)
                    }
                },
                onActiveAction = {
                    if (bindError) addSource(error.liveData) { actionOnError.invoke(it) }
                    textWatcher = onTextChangedWatcher { changeValueConsumer.accept(it.toString()) }
                    editText.addTextChangedListener(textWatcher)
                },
                onInactiveAction = {
                    if (bindError) removeSource(error.liveData)
                    textWatcher?.let { editText.removeTextChangedListener(it) }
                    textWatcher = null
                }
            )
        }

    }

}

@Suppress("unused")
@RvmDslMarker
fun RVM.inputControl(
    initialText: String = "",
    hideErrorOnUserInput: Boolean = true,
    formatter: FormatterAction? = null,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmPropertiesSupport, InputControl> = RvmPropertyReadOnlyDelegate(
    property = InputControl(
        initialText = initialText,
        hideErrorOnUserInput = hideErrorOnUserInput,
        formatter = formatter,
        initialEnabled = initialEnabled,
        initialVisibility = initialVisibility
    )
)

@RvmDslMarker
fun SavedStateHandle.inputControl(
    initialText: String = "",
    hideErrorOnUserInput: Boolean = true,
    formatter: FormatterAction? = null,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmViewModelComponent, InputControl> = delegate { thisRef, stateHandle, key ->
    val textKey = "$key.text"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = InputControl(
        initialText = stateHandle[textKey] ?: initialText,
        hideErrorOnUserInput = hideErrorOnUserInput,
        formatter = formatter,
        initialEnabled = stateHandle[enabledKey] ?: initialEnabled,
        initialVisibility = stateHandle[visibilityKey] ?: initialVisibility
    )
    thisRef.run {
        control.data.viewFlowable
            .subscribe { stateHandle[textKey] = it }
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
    }
    control
}

private fun onTextChangedWatcher(
    action: (CharSequence) -> Unit
): TextWatcher = object : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        /* nothing */
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        /* nothing */
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null) action.invoke(s)
    }

}
