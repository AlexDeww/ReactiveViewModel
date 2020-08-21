package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.CompoundButton
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

@SuppressLint("CheckResult")
class CheckControl internal constructor(
    initialChecked: Boolean,
    initialEnabled: Boolean,
    initialVisibility: Visibility
) : BaseVisualControl<Boolean>(initialChecked, initialEnabled, initialVisibility)

fun checkControl(
    initialChecked: Boolean = false,
    initialEnabled: Boolean = true,
    initialVisibility: BaseVisualControl.Visibility = BaseVisualControl.Visibility.VISIBLE
): CheckControl = CheckControl(
    initialChecked = initialChecked,
    initialEnabled = initialEnabled,
    initialVisibility = initialVisibility
)

fun CheckControl.bindTo(
    compoundButton: CompoundButton,
    bindEnable: Boolean = true,
    bindVisible: Boolean = true
): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(defaultBindTo(compoundButton, bindEnable, bindVisible))
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
