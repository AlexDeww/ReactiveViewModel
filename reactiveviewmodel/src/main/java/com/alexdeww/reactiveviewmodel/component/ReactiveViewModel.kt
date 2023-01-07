package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport
import com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewModelComponent
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

    @CallSuper
    override fun onCleared() {
        rvmAutoDisposableStore.dispose()
        super.onCleared()
    }

    final override fun Disposable.autoDispose(
        tagKey: String,
        storeKey: RvmAutoDisposableSupport.StoreKey?
    ) = rvmAutoDisposableStore.run { this@autoDispose.autoDispose(tagKey, storeKey) }

    final override val <T : Any> RvmProperty<T>.consumer: Consumer<T>
        get() = (this@ReactiveViewModel as RvmPropertiesSupport).run { consumer }
    final override val <T : Any> RvmPropertyBase<T>.observable: Observable<T>
        get() = (this@ReactiveViewModel as RvmPropertiesSupport).run { observable }

    final override fun <T : Any, R> R.call(value: T) where R : RvmCallableProperty<T>,
                                                           R : RvmProperty<T> {
        (this@ReactiveViewModel as RvmPropertiesSupport).run { call(value) }
    }

    final override fun <R> R.call() where R : RvmCallableProperty<Unit>,
                                          R : RvmProperty<Unit> {
        (this@ReactiveViewModel as RvmPropertiesSupport).run { call() }
    }

    final override fun <T : Any, R> R.setValue(value: T) where R : RvmMutableValueProperty<T>,
                                                               R : RvmProperty<T> {
        (this@ReactiveViewModel as RvmPropertiesSupport).run { setValue(value) }
    }

    final override fun <T : Any, R> R.setValueIfChanged(value: T) where R : RvmMutableValueProperty<T>,
                                                                        R : RvmProperty<T> {
        (this@ReactiveViewModel as RvmPropertiesSupport).run { setValueIfChanged(value) }
    }

    final override fun <T : Any> RvmAction<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) {
        (this@ReactiveViewModel as RvmViewModelComponent).run { bind(transformChainBlock) }
    }

    final override fun <T : Any> RvmState<T>.bind(
        transformChainBlock: Observable<T>.() -> Observable<out Any>
    ) {
        (this@ReactiveViewModel as RvmViewModelComponent).run { bind(transformChainBlock) }
    }

}
