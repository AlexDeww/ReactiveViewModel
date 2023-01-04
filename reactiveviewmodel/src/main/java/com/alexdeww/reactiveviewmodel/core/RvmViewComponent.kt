package com.alexdeww.reactiveviewmodel.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport.StoreKey
import com.alexdeww.reactiveviewmodel.core.property.RvmConfirmationEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmEvent
import com.alexdeww.reactiveviewmodel.core.property.RvmState
import com.alexdeww.reactiveviewmodel.widget.BaseControl
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

    fun <T> LiveData<T>.observe(
        action: OnLiveDataAction<T>
    ): Observer<T> = observe(owner = componentLifecycleOwner, action = action)

    fun <T : Any> RvmState<T>.observe(
        action: OnLiveDataAction<T>
    ): Observer<T> = observe(componentLifecycleOwner, action)

    fun <T : Any> RvmEvent<T>.observe(
        action: OnLiveDataAction<T>
    ): Observer<T> = observe(componentLifecycleOwner, action)

    fun <T : Any> RvmConfirmationEvent<T>.observe(
        action: OnLiveDataAction<T>
    ): Observer<T> = observe(componentLifecycleOwner, action)

    val <B : BaseControl.ViewBinder, C : BaseControl<B>> C.binder: B
        get() = getBinder(this@RvmViewComponent)

}
