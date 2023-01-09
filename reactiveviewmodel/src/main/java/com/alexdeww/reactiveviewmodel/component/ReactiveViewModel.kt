package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewModelComponent
import com.alexdeww.reactiveviewmodel.core.annotation.RvmBinderDslMarker
import com.alexdeww.reactiveviewmodel.core.property.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer

/**
 * Based on RxPM
 * https://github.com/dmdevgo/RxPM
 */

abstract class ReactiveViewModel : ViewModel(), RvmViewModelComponent {

    private val rvmAutoDisposableStore by lazy { DefaultRvmDisposableStore() }
    private val defaultViewModelComponent = object : RvmViewModelComponent {
        override fun Disposable.autoDispose(
            tagKey: String,
            storeKey: RvmAutoDisposableSupport.StoreKey?
        ) = this@ReactiveViewModel.rvmAutoDisposableStore.run {
            this@autoDispose.autoDispose(tagKey, storeKey)
        }
    }

    @CallSuper
    override fun onCleared() {
        rvmAutoDisposableStore.dispose()
        super.onCleared()
    }

    final override fun Disposable.autoDispose(
        tagKey: String,
        storeKey: RvmAutoDisposableSupport.StoreKey?
    ) = defaultViewModelComponent.run { autoDispose(tagKey, storeKey) }

    final override val <T : Any> RvmProperty<T>.consumer: Consumer<T>
        get() = defaultViewModelComponent.run { consumer }
    final override val <T : Any> RvmPropertyBase<T>.observable: Observable<T>
        get() = defaultViewModelComponent.run { observable }

    final override fun <T : Any, R> R.call(value: T) where R : RvmCallableProperty<T>,
                                                           R : RvmProperty<T> {
        defaultViewModelComponent.run { call(value) }
    }

    final override fun <R> R.call() where R : RvmCallableProperty<Unit>,
                                          R : RvmProperty<Unit> {
        defaultViewModelComponent.run { call() }
    }

    final override fun <T : Any, R> R.setValue(value: T) where R : RvmMutableValueProperty<T>,
                                                               R : RvmProperty<T> {
        defaultViewModelComponent.run { setValue(value) }
    }

    final override fun <T : Any, R> R.setValueIfChanged(value: T) where R : RvmMutableValueProperty<T>,
                                                                        R : RvmProperty<T> {
        defaultViewModelComponent.run { setValueIfChanged(value) }
    }

    @RvmBinderDslMarker
    final override fun <T : Any> RvmAction<T>.bind(
        chainBlock: Observable<T>.() -> Observable<out Any>
    ) {
        defaultViewModelComponent.run { bind(chainBlock) }
    }

    @RvmBinderDslMarker
    final override fun <T : Any> RvmState<T>.bind(
        chainBlock: Observable<T>.() -> Observable<out Any>
    ) {
        defaultViewModelComponent.run { bind(chainBlock) }
    }

}
