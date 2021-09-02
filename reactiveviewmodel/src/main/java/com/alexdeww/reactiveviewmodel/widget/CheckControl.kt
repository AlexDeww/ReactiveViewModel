package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.CompoundButton
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent

@SuppressLint("CheckResult")
class CheckControl internal constructor(
    initialChecked: Boolean,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<Boolean>(initialChecked, initialEnabled, initialVisibility)

fun checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): CheckControl = CheckControl(
    initialChecked = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
)

fun CheckControl.bindTo(
    rvmViewComponent: RvmViewComponent,
    compoundButton: CompoundButton,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
) = baseBindTo(
    rvmViewComponent = rvmViewComponent,
    view = compoundButton,
    bindEnable = bindEnable,
    bindVisible = bindVisible,
    onValueChanged = { compoundButton.isChecked = it },
    onActiveAction = {
        compoundButton.setOnCheckedChangeListener { _, isChecked ->
            changeValueConsumer.accept(isChecked)
        }
    },
    onInactiveAction = { compoundButton.setOnCheckedChangeListener(null) }
)
