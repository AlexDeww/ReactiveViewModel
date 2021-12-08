package com.alexdeww.reactiveviewmodel.core.property

import androidx.annotation.MainThread
import androidx.collection.ArraySet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.ConfirmationEvent.ObserverWrapper
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer

class ConfirmationEvent<T : Any> internal constructor(debounceInterval: Long? = null) {

    private sealed class EventType {
        data class Pending(val data: Any) : EventType()
        object Confirmed : EventType()
    }

    private val eventState = State<EventType>(EventType.Confirmed, debounceInterval)

    internal val consumer: Consumer<T> = Consumer {
        eventState.consumer.accept(EventType.Pending(it))
    }

    @Suppress("UNCHECKED_CAST")
    internal val observable: Observable<T> = eventState.observable
        .ofType(EventType.Pending::class.java)
        .map { it.data as T }

    val liveData: LiveData<T> by lazy { ConfirmationEventLiveData() }
    val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }
    val isConfirmed: Boolean get() = eventState.value === EventType.Confirmed

    fun confirm() {
        eventState.consumer.accept(EventType.Confirmed)
    }

    private inner class ConfirmationEventLiveData : MediatorLiveData<T>() {

        private val observers = ArraySet<ConfirmationEvent.ObserverWrapper<T>>()

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
            addSource(eventState.liveData) { value = (it as? EventType.Pending)?.data as T }
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
