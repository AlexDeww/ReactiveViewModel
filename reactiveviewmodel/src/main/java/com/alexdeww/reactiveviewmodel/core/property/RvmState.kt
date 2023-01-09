package com.alexdeww.reactiveviewmodel.core.property

import android.annotation.SuppressLint
import com.alexdeww.reactiveviewmodel.core.livedata.RvmLiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * В основном предназначен для передачи состояния из ViewModel во View.
 *
 * * Передавать данные в [RvmState] могут только наследники
 * [RvmPropertiesSupport][com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport].
 *
 * * Всегда хранит последнее переданное значение.
 *
 * * Каждый подписчик в первую очередь получит последннее сохраненное значение.
 */
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
    override val liveData: RvmLiveData<T> by lazy { StateLiveData() }

    @SuppressLint("CheckResult")
    private inner class StateLiveData : RvmLiveData<T>() {
        init {
            viewFlowable.subscribe { value = it }
        }
    }

}
