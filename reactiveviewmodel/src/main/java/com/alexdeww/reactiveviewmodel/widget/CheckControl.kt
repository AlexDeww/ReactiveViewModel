package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.CompoundButton
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
    invisibleState: Int = View.GONE,
    onVisibleChange: OnVisibleChangeAction? = null
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(defaultBindTo(compoundButton, invisibleState, onVisibleChange))
        add(
            value
                .toViewFlowable()
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
