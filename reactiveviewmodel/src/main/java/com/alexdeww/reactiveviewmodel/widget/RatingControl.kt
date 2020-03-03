package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.RatingBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

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

private val RatingBar.ratingBarChange: Observable<Float>
    get() = Observable
        .create { emitter ->
            setOnRatingBarChangeListener { _, rating, _ -> emitter.onNext(rating) }
            emitter.setCancellable { onRatingBarChangeListener = null }
        }

fun RatingControl.bindTo(ratingBar: RatingBar): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(
            rating
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
                .subscribe(actionChange.consumer)
        )
    }
}