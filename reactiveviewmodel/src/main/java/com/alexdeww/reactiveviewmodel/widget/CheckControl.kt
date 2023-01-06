package com.alexdeww.reactiveviewmodel.widget

import android.widget.CompoundButton
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyReadOnlyDelegate
import kotlin.properties.ReadOnlyProperty

class CheckControl internal constructor(
    initialChecked: Boolean,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<Boolean, CheckControl.Binder>(
    initialValue = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
) {

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : BaseBinder<Boolean, CompoundButton>(rvmViewComponent) {

        override val control: BaseVisualControl<Boolean, *> get() = this@CheckControl

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
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmPropertiesSupport, CheckControl> = RvmPropertyReadOnlyDelegate(
    property = CheckControl(
        initialChecked = initialChecked,
        initialEnabled = initialEnabled,
        initialVisibility = initialVisibility
    )
)

@RvmDslMarker
fun SavedStateHandle.checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmViewModelComponent, CheckControl> = visualControlDelegate(
    initialValue = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility,
    initControl = controlDefaultConstrictor(::CheckControl)
)
