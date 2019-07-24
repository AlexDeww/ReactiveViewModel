package com.alexdeww.reactiveviewmodel.widget

import android.annotation.SuppressLint
import android.widget.CompoundButton
import com.alexdeww.reactiveviewmodel.core.BaseControl
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

@SuppressLint("CheckResult")
class CheckControl internal constructor(
    initialChecked: Boolean
) : BaseControl() {

    val checked = state(initialChecked)

    val actionChange = action<Boolean>()

    init {
        actionChange
            .observable
            .filter { it != checked.value }
            .subscribe(checked.consumer)
    }

}

fun checkControl(initialChecked: Boolean = false): CheckControl = CheckControl(initialChecked)

private val CompoundButton.checkedChanges: Observable<Boolean>
    get() = Observable
        .create { emitter ->
            setOnCheckedChangeListener { _, isChecked -> emitter.onNext(isChecked) }
            emitter.setCancellable { setOnCheckedChangeListener(null) }
        }

fun CheckControl.bindTo(compoundButton: CompoundButton): Disposable {
    var editing = false
    return CompositeDisposable().apply {
        add(
            checked
                .observable
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
                .subscribe(actionChange.consumer)
        )
    }
}