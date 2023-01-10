package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Почти тоже самое, что и [RvmState], но отличается тем,
 * что никто не может передавать данные няпрямую.
 *
 * * Никто не может передавать данные няпрямую.
 * [RvmStateProjection] может получать данные от источников:
 * [Observable], [RvmState], [RvmStateProjection],
 * либо объекта наследника [RvmPropertyBase] и [RvmValueProperty].
 */
class RvmStateProjection<T : Any> internal constructor(
    initialValue: T? = null,
) : RvmPropertyBase<T>(), RvmValueProperty<T> {

    private val subject = when (initialValue) {
        null -> BehaviorSubject.create()
        else -> BehaviorSubject.createDefault(initialValue)
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
