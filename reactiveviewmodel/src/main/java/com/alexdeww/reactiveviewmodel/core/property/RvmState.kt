package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

class RvmState<T : Any> internal constructor(
    initValue: T? = null,
    debounceInterval: Long? = null
) : RvmProperty<T>(), RvmMutableValueProperty<T> {

    private val subject = when (initValue) {
        null -> BehaviorSubject.create()
        else -> BehaviorSubject.createDefault(initValue)
    }
    private val serializedSubject = subject.toSerialized()

    internal var valueChangesHook: ((value: T) -> T)? = null
    override val consumer: Consumer<T> = Consumer { newValue ->
        serializedSubject.onNext(valueChangesHook?.invoke(newValue) ?: newValue)
    }
    override val observable: Observable<T> = serializedSubject.letDebounce(debounceInterval)

    override val value: T? get() = subject.value
    override val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }
    override val liveData: RvmLiveData<T> by lazy { StateLiveData(viewFlowable) }

    inner class Projection<R : Any> internal constructor(
        distinctUntilChanged: Boolean,
        projectionBlock: (value: T, consumer: Consumer<R>) -> Unit
    ) : RvmPropertyInternal<R>(), RvmObservableProperty<R>, RvmValueProperty<R> {
        private val subject = BehaviorSubject.create<R>()
        override val consumer: Consumer<R> = Consumer(subject::onNext)
        override val observable: Observable<R> = subject.run {
            if (distinctUntilChanged) distinctUntilChanged() else this
        }

        override val value: R? get() = subject.value
        override val viewFlowable: Flowable<R> by lazy { observable.toViewFlowable() }
        override val liveData: RvmLiveData<R> by lazy { StateLiveData(viewFlowable) }

        init {
            this@RvmState.observable.subscribe { projectionBlock(it, subject::onNext) }
        }
    }

    @SuppressLint("CheckResult")
    private class StateLiveData<T : Any>(source: Flowable<T>) : RvmLiveData<T>() {
        init {
            source.subscribe { value = it }
        }
    }

}
