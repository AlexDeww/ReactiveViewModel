package com.alexdeww.reactiveviewmodel.level.livedata

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.MainThread
import android.support.v4.util.ArraySet

/**
 * https://github.com/hadilq/LiveEvent/blob/master/lib/src/main/java/com/hadilq/liveevent/LiveEvent.kt
 */

open class LiveEvent<T> : MediatorLiveData<T>() {

    private val observers = ArraySet<ObserverWrapper<T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun removeObserver(observer: Observer<T>) {
        if (observers.remove(observer)) {
            super.removeObserver(observer)
            return
        }

        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val wrapper = iterator.next()
            if (wrapper.observer == observer) {
                iterator.remove()
                super.removeObserver(wrapper)
                break
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.newValue() }
        super.setValue(t)
    }

    private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {

        private var pending = false

        override fun onChanged(t: T?) {
            if (pending) {
                pending = false
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending = true
        }

    }
}