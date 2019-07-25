package com.alexdeww.reactiveviewmodel.core.widget

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.BaseControl

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
