package com.alexdeww.reactiveviewmodel.core

import android.arch.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Based on RxPM
 * https://github.com/dmdevgo/RxPM
 */

abstract class ReactiveViewModel : ViewModel(), RvmComponent {

    private val disposableList = CompositeDisposable()

    override fun onCleared() {
        disposableList.clear()
        super.onCleared()
    }

    fun Disposable.disposeOnCleared(): Disposable {
        disposableList.add(this)
        return this
    }

    protected fun <T> state(initValue: T? = null): State<T> = State(initValue)

    protected fun <T> event(): Event<T> = Event()

    protected fun eventNone(): Event<Unit> = Event()

    protected fun <T> action(): Action<T> = Action()

    protected fun actionNone(): Action<Unit> = Action()

}

