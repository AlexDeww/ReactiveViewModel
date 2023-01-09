package com.alexdeww.reactiveviewmodel.core.property

import androidx.annotation.MainThread
import androidx.collection.ArraySet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.RvmConfirmationEvent.ObserverWrapper
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer

/**
 * Почти тоже самое, что и [RvmEvent], но отличается тем, что хранит последнее значение
 * пока не будет вызван метод [confirm].
 *
 * * Передавать данные в [RvmConfirmationEvent] могут только наследники
 * [RvmPropertiesSupport][com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport].
 *
 * * Хранит последнее переданное значение пока не будет вызван метод [confirm].
 * Каждый новый подписчик будет получать последнее сохраненное значение,
 * пока не вызван метод [confirm].
 */
class RvmConfirmationEvent<T : Any> internal constructor(
    debounceInterval: Long? = null
) : RvmProperty<T>(), RvmCallableProperty<T> {

    private sealed class EventType {
        data class Pending(val data: Any) : EventType()
        object Confirmed : EventType()

        @Suppress("UNCHECKED_CAST")
        fun <T> tryGetData(): T? = if (this is Pending) data as T else null
    }

    private val eventState = RvmState<EventType>(EventType.Confirmed, debounceInterval)

    override val consumer: Consumer<T> = Consumer {
        eventState.consumer.accept(EventType.Pending(it))
    }

    @Suppress("UNCHECKED_CAST")
    override val observable: Observable<T> = eventState.observable
        .ofType(EventType.Pending::class.java)
        .map { it.data as T }

    override val liveData: LiveData<T> by lazy { ConfirmationEventLiveData() }
    override val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }
    val isConfirmed: Boolean get() = eventState.value === EventType.Confirmed

    /**
     * Подтверждение, что данные получены
     */
    fun confirm() {
        if (!isConfirmed) eventState.consumer.accept(EventType.Confirmed)
    }

    private inner class ConfirmationEventLiveData : MediatorLiveData<T>() {

        private val observers = ArraySet<RvmConfirmationEvent.ObserverWrapper<T>>()

        @MainThread
        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            val wrapper = ObserverWrapper(observer)
            observers.add(wrapper)
            super.observe(owner, wrapper)
        }

        @MainThread
        override fun removeObserver(observer: Observer<in T>) {
            if (observers.remove(observer as Observer<*>)) {
                super.removeObserver(observer)
                return
            }

            val iterator = observers.iterator()
            while (iterator.hasNext()) {
                val wrapper = iterator.next()
                if (wrapper.observer == observer) {
                    iterator.remove()
                    super.removeObserver(observer)
                    break
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun onActive() {
            super.onActive()
            addSource(eventState.liveData) { value = it.tryGetData<T>() }
        }

        override fun onInactive() {
            removeSource(eventState.liveData)
            super.onInactive()
        }
    }

    private class ObserverWrapper<T>(val observer: Observer<in T>) : Observer<T> {
        override fun onChanged(t: T?) {
            if (t != null) observer.onChanged(t)
        }
    }

}
