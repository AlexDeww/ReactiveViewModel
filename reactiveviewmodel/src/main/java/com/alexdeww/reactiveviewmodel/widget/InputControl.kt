package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.support.design.widget.TextInputLayout
import android.text.*
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

typealias FormatterAction = (text: String) -> String

@SuppressLint("CheckResult")
class InputControl internal constructor(
    initialText: String,
    formatter: FormatterAction?,
    hideErrorOnUserInput: Boolean
) : BaseControl() {

    val text = state(initialText)
    val error = state<String>()

    val actionChangeText = action<String>()

    init {
        actionChangeText
            .observable
            .filter { it != text.value }
            .map { s -> formatter?.let { it(s) } ?: s }
            .subscribe {
                text.consumer.accept(it)
                if (hideErrorOnUserInput) error.consumer.accept("")
            }
    }

}

fun inputControl(
    initialText: String = "",
    formatter: FormatterAction? = null,
    hideErrorOnUserInput: Boolean = true
): InputControl = InputControl(initialText, formatter, hideErrorOnUserInput)


private val EditText.textChanges: Observable<CharSequence>
    get() = Observable
        .create { emitter ->
            val listener = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* nothing */
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
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
    useError: Boolean = false
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(
            text
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
                .subscribe(actionChangeText.consumer)
        )
    }
}

fun InputControl.bindTo(
    textInputLayout: TextInputLayout,
    useError: Boolean = false
): Disposable = CompositeDisposable().apply {
    add(bindTo(textInputLayout.editText!!, false))
    if (useError) {
        add(
            error
                .observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s -> textInputLayout.error = s.takeIf { it.isNotEmpty() } }
        )
    }
}