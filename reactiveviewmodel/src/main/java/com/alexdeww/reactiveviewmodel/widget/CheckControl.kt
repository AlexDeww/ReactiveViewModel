package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.CompoundButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

@SuppressLint("CheckResult")
class CheckControl internal constructor(
    initialChecked: Boolean
) : BaseVisualControl<Boolean>(initialChecked)

fun checkControl(initialChecked: Boolean = false): CheckControl = CheckControl(initialChecked)

fun CheckControl.bindTo(
    compoundButton: CompoundButton,
    invisibleState: Int = View.GONE
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(commonBindTo(compoundButton, invisibleState))
        add(
            value
                .observable
                .toFlowable(BackpressureStrategy.LATEST)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    editing = true
                    compoundButton.isChecked = it
                    editing = false
                }
        )
        add(
            compoundButton
                .checkedChanges
                .filter { !editing }
                .subscribe(actionChangeValue.consumer)
        )
    }
}

private val CompoundButton.checkedChanges: Observable<Boolean>
    get() = Observable.create { emitter ->
        setOnCheckedChangeListener { _, isChecked -> emitter.onNext(isChecked) }
        emitter.setCancellable { setOnCheckedChangeListener(null) }
    }
