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

typealias OnVisibleChangeAction = (isVisible: Boolean) -> Unit

abstract class BaseVisualControl<T>(
    initialValue: T
) : BaseControl() {

    val value = state(initialValue)
    val isEnabled = state(true)
    val isVisible = state(true)

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
        invisibleState: Int = View.GONE,
        onVisibleChange: OnVisibleChangeAction? = null
    ): Disposable = CompositeDisposable().apply {
        add(
            isEnabled
                .toViewFlowable()
                .subscribe { view.isEnabled = it }
        )

        add(
            isVisible
                .toViewFlowable()
                .subscribe {
                    when {
                        onVisibleChange != null -> onVisibleChange.invoke(it)
                        else -> view.visibility = if (it) View.VISIBLE else invisibleState
                    }
                }
        )
    }

    protected open fun transformObservable(observable: Observable<T>): Observable<T> = observable

    @CallSuper
    protected open fun onChangedValue(newValue: T) {
        value.consumer.accept(newValue)
    }

}
