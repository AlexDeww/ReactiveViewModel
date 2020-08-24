package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.RatingBar
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent

@SuppressLint("CheckResult")
class RatingControl internal constructor(
    initialValue: Float,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<Float>(initialValue, initialEnabled, initialVisibility)

fun ratingControl(
    initialValue: Float = 0f,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): RatingControl = RatingControl(
    initialValue = initialValue,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
)

fun RatingControl.bindTo(
    rvmViewComponent: RvmViewComponent,
    ratingBar: RatingBar,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
) = baseBindTo(
    rvmViewComponent = rvmViewComponent,
    view = ratingBar,
    bindEnable = bindEnable,
    bindVisible = bindVisible,
    onValueChanged = { ratingBar.rating = it },
    onActiveAction = {
        ratingBar.setOnRatingBarChangeListener { _, rating, _ -> changeValueConsumer.accept(rating) }
    },
    onInactiveAction = { ratingBar.onRatingBarChangeListener = null }
)
