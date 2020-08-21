package com.alexdeww.reactiveviewmodel.widget

import android.view.View
import androidx.annotation.CallSuper
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

abstract class BaseVisualControl<T>(
    initialValue: T,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseControl() {

    enum class Visibility(val value: Int) {
        VISIBLE(View.VISIBLE),
        INVISIBLE(View.INVISIBLE),
        GONE(View.GONE)
    }

    val value = state(initialValue)
    val enabled = state(initialEnabled)
    val visibility = state(initialVisibility)

    val actionChangeValue = action<T>()

    init {
        actionChangeValue
            .observable
            .filter { it != value.value }
            .let { transformObservable(it) }
            .filter { it != value.value }
            .subscribe(::onChangedValue)
    }

    fun <T> State<T>.toViewFlowable(): Flowable<T> = this
        .observable
        .toFlowable(BackpressureStrategy.LATEST)
        .observeOn(AndroidSchedulers.mainThread())

    fun defaultBindTo(
        view: View,
        bindEnable: Boolean,
        bindVisible: Boolean
    ): Disposable = CompositeDisposable().apply {
        if (bindEnable) {
            add(
                enabled
                    .toViewFlowable()
                    .subscribe { view.isEnabled = it }
            )
        }

        if (bindVisible) {
            add(
                visibility
                    .toViewFlowable()
                    .subscribe { view.visibility = it.value }
            )
        }
    }

    protected open fun transformObservable(observable: Observable<T>): Observable<T> = observable

    @CallSuper
    protected open fun onChangedValue(newValue: T) {
        value.consumer.accept(newValue)
    }

}
