package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.common.RvmComponent
import com.alexdeww.reactiveviewmodel.core.property.Action
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

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

    protected fun <T : Any> state(initValue: T? = null, debounceInterval: Long? = null): State<T> =
        State(initValue, debounceInterval)

    protected fun <T : Any> event(debounceInterval: Long? = null): Event<T> =
        Event(debounceInterval)

    protected fun eventNone(debounceInterval: Long? = null): Event<Unit> =
        Event(debounceInterval)

    protected fun <T : Any> action(debounceInterval: Long? = null): Action<T> =
        Action(debounceInterval)

    protected fun actionNone(debounceInterval: Long? = null): Action<Unit> =
        Action(debounceInterval)

}
