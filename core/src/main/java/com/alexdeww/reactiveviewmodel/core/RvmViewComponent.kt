package com.alexdeww.reactiveviewmodel.core

import com.alexdeww.reactiveviewmodel.core.property.Event
import com.alexdeww.reactiveviewmodel.core.property.State
import com.alexdeww.reactiveviewmodel.level.ApiLifecycleOwner
import com.alexdeww.reactiveviewmodel.level.ApiLiveData
import com.alexdeww.reactiveviewmodel.level.ApiObserver
import io.reactivex.disposables.Disposable

interface RvmViewComponent {

    val componentLifecycleOwner: ApiLifecycleOwner

    fun Disposable.disposeOnDestroy(tag: String)

    fun Disposable.disposeOnStop(tag: String)

    fun Disposable.disposeOnDestroyView(tag: String)

    fun <T> ApiLiveData<T>.observe(action: OnLiveDataAction<T>): ApiObserver<T> =
        observe(componentLifecycleOwner, action)

    fun <T> State<T>.observe(action: OnLiveDataAction<T>): ApiObserver<T> =
        observe(componentLifecycleOwner, action)

    fun <T> Event<T>.observe(action: OnLiveDataAction<T>): ApiObserver<T> =
        observe(componentLifecycleOwner, action)

}