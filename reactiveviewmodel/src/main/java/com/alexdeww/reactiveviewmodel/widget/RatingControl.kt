package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.RatingBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

@SuppressLint("CheckResult")
class RatingControl internal constructor(
    initialValue: Float
) : BaseVisualControl<Float>(initialValue)

fun ratingControl(initialValue: Float = 0f): RatingControl = RatingControl(initialValue)

private val RatingBar.ratingBarChange: Observable<Float>
    get() = Observable
        .create { emitter ->
            setOnRatingBarChangeListener { _, rating, _ -> emitter.onNext(rating) }
            emitter.setCancellable { onRatingBarChangeListener = null }
        }

fun RatingControl.bindTo(
    ratingBar: RatingBar,
    invisibleState: Int = View.GONE
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(commonBindTo(ratingBar, invisibleState))
        add(
            value
                .observable
                .observeOn(AndroidSchedulers.mainThread())
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