package com.alexdeww.reactiveviewmodel.core

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.livedata.LiveEvent
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

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
    protected fun emptyEvent(): Event<Unit> = Event()
    protected fun <T> action(): Action<T> = Action()
    protected fun emptyAction(): Action<Unit> = Action()

}

class State<T> internal constructor(initValue: T? = null) {

    private val subject = if (initValue == null) {
        BehaviorSubject.create<T>()
    } else {
        BehaviorSubject.createDefault<T>(initValue)
    }
    private val serializedSubject = subject.toSerialized()

    internal val consumer: Consumer<T> = Consumer { serializedSubject.onNext(it) }

    val value: T? get() = subject.value
    val valueNonNull: T get() = value!!
    val hasValue: Boolean get() = value != null

    val liveData: RvmLiveData<T> by lazy { StateLiveData() }
    val observable: Observable<T> = serializedSubject

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()
    fun getValueOrDef(defValue: T): T = getValueOrDef { defValue }

    @SuppressLint("CheckResult")
    private inner class StateLiveData : RvmLiveData<T>() {
        init {
            serializedSubject.subscribe { postValue(it) }
        }
    }

}

class Action<T> internal constructor() {

    private val subject = PublishSubject.create<T>().toSerialized()

    internal val observable: Observable<T> = subject

    val consumer: Consumer<T> = Consumer { subject.onNext(it) }

    fun call(value: T) {
        consumer.accept(value)
    }

}

class Event<T> internal constructor() {

    private val subject = BehaviorSubject.create<T>()
    private val serializedSubject = subject.toSerialized()
    private val isPending = AtomicBoolean(false)

    internal val consumer: Consumer<T> = Consumer {
        isPending.set(true)
        serializedSubject.onNext(it)
    }

    val observable: Observable<T> = Observable
        .create<T> { emitter ->
            val skipCount = if (!isPending.get() && subject.hasValue()) 1L else 0L
            val d = serializedSubject.skip(skipCount).subscribe {
                isPending.set(false)
                emitter.onNext(it)
            }
            emitter.setCancellable { d.dispose() }
        }
        .share()
    val liveData: LiveData<T> by lazy { EventLiveData() }

    private inner class EventLiveData : LiveEvent<T>() {

        private var disposable: Disposable? = null

        override fun onActive() {
            super.onActive()
            disposable = observable.subscribe { postValue(it) }
        }

        override fun onInactive() {
            disposable?.dispose()
            super.onInactive()
        }

    }

}

