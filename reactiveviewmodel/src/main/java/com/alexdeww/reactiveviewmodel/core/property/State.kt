package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

class State<T : Any> internal constructor(
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

    val liveData: RvmLiveData<T> by lazy { StateLiveData() }
    val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()
    fun getValueOrDef(defValue: T): T = getValueOrDef { defValue }

    @SuppressLint("CheckResult")
    private inner class StateLiveData : RvmLiveData<T>() {
        init {
            viewFlowable.subscribe { value = it }
        }
    }

}