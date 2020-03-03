package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.text.*
import android.view.View
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

typealias FormatterAction = (text: String) -> String

@SuppressLint("CheckResult")
class InputControl internal constructor(
    initialText: String,
    private val hideErrorOnUserInput: Boolean,
    private val formatter: FormatterAction?
) : BaseVisualControl<String>(initialText) {

    val error = state<String>()

    override fun transformObservable(
        observable: Observable<String>
    ): Observable<String> = observable
        .map { s -> formatter?.let { it(s) } ?: s }

    override fun onChangedValue(newValue: String) {
        super.onChangedValue(newValue)
        if (hideErrorOnUserInput) error.consumer.accept("")
    }

}

fun inputControl(
    initialText: String = "",
    hideErrorOnUserInput: Boolean = true,
    formatter: FormatterAction? = null
): InputControl = InputControl(initialText, hideErrorOnUserInput, formatter)


private val EditText.textChanges: Observable<CharSequence>
    get() = Observable
        .create { emitter ->
            val listener = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* nothing */
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    /* nothing */
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) emitter.onNext(s)
                }
            }
            addTextChangedListener(listener)
            emitter.setCancellable { removeTextChangedListener(listener) }
        }

fun InputControl.bindTo(
    editText: EditText,
    useError: Boolean = false,
    invisibleState: Int = View.GONE
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(commonBindTo(editText, invisibleState))
        add(
            value
                .observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val editable = editText.text
                    if (!it!!.contentEquals(editable)) {
                        editing = true
                        if (editable is Spanned) {
                            val ss = SpannableString(it)
                            TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                            editable.replace(0, editable.length, ss)
                        } else {
                            editable.replace(0, editable.length, it)
                        }
                        editing = false
                    }
                }
        )

        if (useError) {
            add(
                error
                    .observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { s -> editText.error = s.takeIf { it.isNotEmpty() } }
            )
        }

        add(
            editText
                .textChanges
                .filter { !editing }
                .map { it.toString() }
                .subscribe(actionChangeValue.consumer)
        )
    }
}

fun InputControl.bindTo(
    textInputLayout: TextInputLayout,
    useError: Boolean = false,
    invisibleState: Int = View.GONE
): Disposable = CompositeDisposable().apply {
    add(bindTo(textInputLayout.editText!!, false, invisibleState))
    if (useError) {
        add(
            error
                .observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s -> textInputLayout.error = s.takeIf { it.isNotEmpty() } }
        )
    }
}