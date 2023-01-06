package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.RatingBar
import androidx.lifecycle.SavedStateHandle
import com.alexdeww.reactiveviewmodel.core.*
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.annotation.RvmDslMarker
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
): ReadOnlyProperty<RvmPropertiesSupport, RatingControl> = RvmPropertyReadOnlyDelegate(
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
): ReadOnlyProperty<RvmViewModelComponent, RatingControl> = visualControlDelegate(
    initialValue = initialValue,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility,
    initControl = controlDefaultConstrictor(::RatingControl)
)
