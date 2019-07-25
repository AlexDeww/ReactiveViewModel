package com.alexdeww.reactiveviewmodel.core.widget

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.BaseControl

@SuppressLint("CheckResult")
class RatingControl internal constructor(
    initialValue: Float
) : BaseControl() {

    val rating = state(initialValue)

    val actionChange = action<Float>()

    init {
        actionChange
            .observable
            .filter { it != rating.value }
            .subscribe { rating.consumer.accept(it) }
    }

}

fun ratingControl(initialValue: Float = 0f): RatingControl = RatingControl(initialValue)