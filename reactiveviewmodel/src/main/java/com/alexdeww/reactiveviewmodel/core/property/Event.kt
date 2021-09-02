package com.alexdeww.reactiveviewmodel.core.property

import androidx.lifecycle.LiveData
import com.alexdeww.reactiveviewmodel.core.livedata.LiveEvent
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicBoolean

class Event<T : Any> internal constructor(debounceInterval: Long? = null) {

    private val subject = BehaviorSubject.create<T>()
    private val serializedSubject = subject.toSerialized()
    private val isPending = AtomicBoolean(false)

    internal val consumer: Consumer<T> = Consumer {
        isPending.set(true)
        serializedSubject.onNext(it)
    }

    internal val observable: Observable<T> = Observable
        .create<T> { emitter ->
            val skipCount = if (!isPending.get() && subject.hasValue()) 1L else 0L
            val d = serializedSubject.skip(skipCount).subscribe {
                isPending.set(false)
                emitter.onNext(it)
            }
            emitter.setCancellable { d.dispose() }
        }
        .letDebounce(debounceInterval)
        .share()

    val liveData: LiveData<T> by lazy { EventLiveData() }
    val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }

    private inner class EventLiveData : LiveEvent<T>() {

        private var disposable: Disposable? = null

        override fun onActive() {
            super.onActive()
            disposable = viewFlowable.subscribe { value = it }
        }

        override fun onInactive() {
            disposable?.dispose()
            super.onInactive()
        }

    }

}