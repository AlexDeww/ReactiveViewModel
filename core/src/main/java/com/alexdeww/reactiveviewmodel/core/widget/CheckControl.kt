package com.alexdeww.reactiveviewmodel.core.widget

import android.annotation.SuppressLint

@SuppressLint("CheckResult")
class CheckControl internal constructor(
    initialChecked: Boolean
) : BaseControl() {

    val checked = state(initialChecked)

    val actionChange = action<Boolean>()

    init {
        actionChange
            .observable
            .filter { it != checked.value }
            .subscribe(checked.consumer)
    }

}

fun checkControl(initialChecked: Boolean = false): CheckControl = CheckControl(initialChecked)
