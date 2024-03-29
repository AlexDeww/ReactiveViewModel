package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport.StoreKey
import com.alexdeww.reactiveviewmodel.core.property.RvmObservableProperty
import com.alexdeww.reactiveviewmodel.widget.RvmBaseControl
import io.reactivex.rxjava3.disposables.Disposable

interface RvmViewComponent : RvmAutoDisposableSupport {

    companion object {
        val onStopStoreKey = StoreKey("RvmViewComponent.onStopStoreKey")
        val onDestroyViewStoreKey = StoreKey("RvmViewComponent.onDestroyViewStoreKey")
    }

    val componentLifecycleOwner: LifecycleOwner

    fun Disposable.disposeOnStop(tag: String) = autoDispose(tag, onStopStoreKey)
    fun Disposable.disposeOnDestroyView(tag: String) = autoDispose(tag, onDestroyViewStoreKey)
    fun Disposable.disposeOnDestroy(tag: String) = autoDispose(tag)

    fun <T> LiveData<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(owner = componentLifecycleOwner, action = action)

    fun <T : Any> RvmObservableProperty<T>.observe(action: OnLiveDataAction<T>): Observer<T> =
        observe(componentLifecycleOwner, action)

    val <B : RvmBaseControl.ViewBinder, C : RvmBaseControl<B>> C.binder: B
        get() = getBinder(this@RvmViewComponent)

}
