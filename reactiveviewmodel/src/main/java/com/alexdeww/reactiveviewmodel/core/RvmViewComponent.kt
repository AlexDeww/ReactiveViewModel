package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import io.reactivex.rxjava3.disposables.Disposable

interface RvmViewComponent {

    val componentLifecycleOwner: LifecycleOwner

    fun Disposable.disposeOnDestroy(tag: String)

    fun Disposable.disposeOnStop(tag: String)

    fun Disposable.disposeOnDestroyView(tag: String)

    fun <T> LiveData<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T> State<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T> Event<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

}