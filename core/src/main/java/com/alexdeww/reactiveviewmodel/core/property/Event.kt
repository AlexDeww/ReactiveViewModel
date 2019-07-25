package com.alexdeww.reactiveviewmodel.core.property

import com.alexdeww.reactiveviewmodel.level.ApiLiveData
import com.alexdeww.reactiveviewmodel.level.livedata.LiveEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicBoolean

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
    val liveData: ApiLiveData<T> by lazy { EventLiveData() }

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