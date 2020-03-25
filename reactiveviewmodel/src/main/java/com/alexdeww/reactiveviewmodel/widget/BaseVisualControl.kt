package com.alexdeww.reactiveviewmodel.widget

import android.view.View
import androidx.annotation.CallSuper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

abstract class BaseVisualControl<T>(
    initialValue: T
) : BaseControl() {

    val value = state(initialValue)
    val isEnabled = state(true)
    val isVisible = state(true)

    val actionChangeValue = action<T>()

    protected open fun transformObservable(observable: Observable<T>): Observable<T> = observable

    @CallSuper
    protected open fun onChangedValue(newValue: T) {
        value.consumer.accept(newValue)
    }

    init {
        actionChangeValue
            .observable
            .filter { it != value.value }
            .let { transformObservable(it) }
            .subscribe(::onChangedValue)
    }

}

internal fun <T> BaseVisualControl<T>.commonBindTo(
    view: View,
    invisibleState: Int
): Disposable = CompositeDisposable().apply {
    add(
        isEnabled
            .observable
            .toFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it != view.isEnabled }
            .subscribe { view.isEnabled = it }
    )

    add(
        isVisible
            .observable
            .toFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it != (view.visibility == View.VISIBLE) }
            .subscribe { view.visibility = if (it) View.VISIBLE else invisibleState }
    )
}