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
) {

    private val subject = when (initValue) {
        null -> BehaviorSubject.create()
        else -> BehaviorSubject.createDefault(initValue)
    }
    private val serializedSubject = subject.toSerialized()

    internal var valueChangesHook: ((value: T) -> T)? = null
    internal val consumer: Consumer<T> = Consumer { newValue ->
        serializedSubject.onNext(valueChangesHook?.invoke(newValue) ?: newValue)
    }
    internal val observable: Observable<T> = serializedSubject.letDebounce(debounceInterval)

    val value: T? get() = subject.value
    val valueNonNull: T get() = value!!
    val hasValue: Boolean get() = value != null

    val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }
    val liveData: RvmLiveData<T> by lazy { StateLiveData(viewFlowable) }

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()
    fun getValueOrDef(defValue: T): T = getValueOrDef { defValue }

    inner class Projection<R : Any> internal constructor(
        distinctUntilChanged: Boolean,
        projectionBlock: (value: T, consumer: Consumer<R>) -> Unit
    ) {
        private val projectionSubject = BehaviorSubject.create<R>()
        private val projectionSource = projectionSubject.run {
            if (distinctUntilChanged) distinctUntilChanged()
            else this
        }

        val value: R? get() = projectionSubject.value
        val valueNonNull: R get() = value!!
        val hasValue: Boolean get() = value != null

        val viewFlowable: Flowable<R> by lazy { projectionSource.toViewFlowable() }
        val liveData: RvmLiveData<R> by lazy { StateLiveData(viewFlowable) }

        fun getValueOrDef(actionDefValue: () -> R): R = value ?: actionDefValue()
        fun getValueOrDef(defValue: R): R = getValueOrDef { defValue }

        init {
            observable.subscribe { projectionBlock(it, projectionSubject::onNext) }
        }
    }

    @SuppressLint("CheckResult")
    private class StateLiveData<T : Any>(source: Flowable<T>) : RvmLiveData<T>() {
        init {
            source.subscribe { value = it }
        }
    }

}
