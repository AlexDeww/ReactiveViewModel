package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

class RvmStateProjection<T : Any> internal constructor(
    initValue: T? = null,
) : RvmPropertyBase<T>(), RvmValueProperty<T> {

    private val subject = when (initValue) {
        null -> BehaviorSubject.create()
        else -> BehaviorSubject.createDefault(initValue)
    }
    private val serializedSubject = subject.toSerialized()

    override val consumer: Consumer<T> = Consumer(serializedSubject::onNext)
    override val observable: Observable<T> = serializedSubject

    override val value: T? get() = subject.value
    override val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }
    override val liveData: RvmLiveData<T> by lazy { StateLiveData() }

    @SuppressLint("CheckResult")
    private inner class StateLiveData : RvmLiveData<T>() {
        init {
            viewFlowable.subscribe { value = it }
        }
    }

}
