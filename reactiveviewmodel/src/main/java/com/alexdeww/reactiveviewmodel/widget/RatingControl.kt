package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.RatingBar
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.component.ReactiveViewModel
import com.alexdeww.reactiveviewmodel.core.RVM
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.alexdeww.reactiveviewmodel.core.RvmWidgetsSupport
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
import com.alexdeww.reactiveviewmodel.core.delegate
import com.alexdeww.reactiveviewmodel.core.utils.RvmPropertyReadOnlyDelegate
import kotlin.properties.ReadOnlyProperty

@SuppressLint("CheckResult")
class RatingControl internal constructor(
    initialValue: Float,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<Float, RatingControl.Binder>(
    initialValue = initialValue,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
) {

    override fun getBinder(rvmViewComponent: RvmViewComponent): Binder = Binder(rvmViewComponent)

    inner class Binder internal constructor(
        rvmViewComponent: RvmViewComponent
    ) : BaseBinder<Float, RatingBar>(rvmViewComponent) {

        override val control: BaseVisualControl<Float, *> get() = this@RatingControl

        @RvmBinderDslMarker
        fun bindTo(
            ratingBar: RatingBar,
            bindEnable: Boolean = true,
            bindVisible: Boolean = true
        ) = bindTo(
            view = ratingBar,
            bindEnable = bindEnable,
            bindVisible = bindVisible,
            onValueChanged = { ratingBar.rating = it },
            onActiveAction = {
                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    changeValueConsumer.accept(rating)
                }
            },
            onInactiveAction = { ratingBar.onRatingBarChangeListener = null }
        )

    }

}

@Suppress("unused")
@RvmDslMarker
fun RVM.ratingControl(
    initialValue: Float = 0f,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<RvmWidgetsSupport, RatingControl> = RvmPropertyReadOnlyDelegate(
    property = RatingControl(
        initialValue = initialValue,
        initialEnabled = initialEnabled,
        initialVisibility = initialVisibility
    )
)

@RvmDslMarker
fun SavedStateHandle.ratingControl(
    initialValue: Float = 0f,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): ReadOnlyProperty<ReactiveViewModel, RatingControl> = delegate { thisRef, stateHandle, key ->
    val ratingKey = "$key.rating"
    val enabledKey = "$key.enabled"
    val visibilityKey = "$key.visibility"
    val control = RatingControl(
        initialValue = stateHandle[ratingKey] ?: initialValue,
        initialEnabled = stateHandle[enabledKey] ?: initialEnabled,
        initialVisibility = stateHandle[visibilityKey] ?: initialVisibility
    )
    thisRef.run {
        control.value.viewFlowable
            .subscribe { stateHandle[ratingKey] = it }
            .autoDispose()
        control.enabled.viewFlowable
            .subscribe { stateHandle[enabledKey] = it }
            .autoDispose()
        control.visibility.viewFlowable
            .subscribe { stateHandle[visibilityKey] = it }
            .autoDispose()
    }
    control
}
