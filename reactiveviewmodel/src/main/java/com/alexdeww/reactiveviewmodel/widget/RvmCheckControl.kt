package com.alexdeww.reactiveviewmodel.widget

import android.widget.CompoundButton
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyDelegate
import kotlin.properties.ReadOnlyProperty

class RvmCheckControl internal constructor(
    initialChecked: Boolean,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : RvmBaseVisualControl<Boolean, RvmCheckControl.Binder>(
    initialValue = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
) {

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : BaseBinder<Boolean, CompoundButton>(rvmViewComponent) {

        override val control: RvmBaseVisualControl<Boolean, *> get() = this@RvmCheckControl

        @RvmBinderDslMarker
        fun bindTo(
            compoundButton: CompoundButton,
            bindEnable: Boolean = true,
            bindVisible: Boolean = true
        ) {
            bindTo(
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
        }

    }

}

@Suppress("unused")
@RvmDslMarker
fun RVM.checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: RvmBaseVisualControl.Visibility = RvmBaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmPropertiesSupport, RvmCheckControl> = RvmPropertyDelegate.def {
    RvmCheckControl(
        initialChecked = initialChecked,
        initialEnabled = initialEnabled,
        initialVisibility = initialVisibility
    )
}

@RvmDslMarker
fun SavedStateHandle.checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: RvmBaseVisualControl.Visibility = RvmBaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmViewModelComponent, RvmCheckControl> = visualControlDelegate(
    initialValue = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility,
    initControl = rvmControlDefaultConstructor(::RvmCheckControl)
)
