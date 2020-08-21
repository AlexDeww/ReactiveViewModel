package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.RatingBar
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

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
    ratingBar: RatingBar,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(defaultBindTo(ratingBar, bindEnable, bindVisible))
        add(
            value
                .toViewFlowable()
                .subscribe {
                    editing = true
                    ratingBar.rating = it
                    editing = false
                }
        )

        add(
            ratingBar
                .ratingBarChange
                .filter { !editing }
                .subscribe(actionChangeValue.consumer)
        )
    }
}

private val RatingBar.ratingBarChange: Observable<Float>
    get() = Observable.create { emitter ->
        setOnRatingBarChangeListener { _, rating, _ -> emitter.onNext(rating) }
        emitter.setCancellable { onRatingBarChangeListener = null }
    }
