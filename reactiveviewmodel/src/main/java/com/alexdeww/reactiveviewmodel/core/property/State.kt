package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

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