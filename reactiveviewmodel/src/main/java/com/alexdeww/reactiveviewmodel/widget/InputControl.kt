package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.text.*
import android.widget.EditText
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

typealias FormatterAction = (text: String) -> String

@SuppressLint("CheckResult")
class InputControl internal constructor(
    initialText: String,
    private val hideErrorOnUserInput: Boolean,
    private val formatter: FormatterAction?,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<String>(initialText, initialEnabled, initialVisibility) {

    val error = state<String>()

    override fun transformObservable(
        observable: Observable<String>
    ): Observable<String> = observable.map { s ->
        formatter?.let { it(s) } ?: s
    }

    override fun onChangedValue(newValue: String) {
        super.onChangedValue(newValue)
        if (hideErrorOnUserInput) error.consumer.accept("")
    }

}

fun inputControl(
    initialText: String = "",
    hideErrorOnUserInput: Boolean = true,
    formatter: FormatterAction? = null,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): InputControl = InputControl(
    initialText = initialText,
    hideErrorOnUserInput = hideErrorOnUserInput,
    formatter = formatter,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
)

fun InputControl.bindTo(
    rvmViewComponent: RvmViewComponent,
    editText: EditText,
    bindError: Boolean = false,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
) = bindTo(
    rvmViewComponent = rvmViewComponent,
    editText = editText,
    actionOnError = { editText.error = it },
    bindError = bindError,
    bindEnable = bindEnable,
    bindVisible = bindVisible
)

fun InputControl.bindTo(
    rvmViewComponent: RvmViewComponent,
    textInputLayout: TextInputLayout,
    bindError: Boolean = false,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
) = bindTo(
    rvmViewComponent = rvmViewComponent,
    editText = textInputLayout.editText!!,
    actionOnError = { textInputLayout.error = it },
    bindError = bindError,
    bindEnable = bindEnable,
    bindVisible = bindVisible
)

internal fun InputControl.bindTo(
    rvmViewComponent: RvmViewComponent,
    editText: EditText,
    actionOnError: (String) -> Unit,
    bindError: Boolean = false,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
) {
    var textWatcher: TextWatcher? = null
    baseBindTo(
        rvmViewComponent = rvmViewComponent,
        view = editText,
        bindEnable = bindEnable,
        bindVisible = bindVisible,
        onValueChanged = { newValue ->
            val editable = editText.text
            if (!newValue.contentEquals(editable)) {
                if (editable is Spanned) {
                    val ss = SpannableString(newValue)
                    TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                    editable.replace(0, editable.length, ss)
                } else {
                    editable.replace(0, editable.length, newValue)
                }
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
