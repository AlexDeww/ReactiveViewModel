package com.alexdeww.reactiveviewmodel.component

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.Event
import com.alexdeww.reactiveviewmodel.core.OnLiveDataAction
import com.alexdeww.reactiveviewmodel.core.State
import com.alexdeww.reactiveviewmodel.core.observe
import io.reactivex.disposables.Disposable

interface RvmAndroidComponent {

    val componentLifecycleOwner: LifecycleOwner

    fun Disposable.disposeOnDestroy(tag: String)

    fun Disposable.disposeOnStop(tag: String)

    fun Disposable.disposeOnDestroyView(tag: String)

    fun <T> LiveData<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    fun <T> State<T>.observe(action: OnLiveDataAction<T>) =
        observe(componentLifecycleOwner, action)

    fun <T> Event<T>.observe(action: OnLiveDataAction<T>) =
        observe(componentLifecycleOwner, action)

}