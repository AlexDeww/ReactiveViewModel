package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewModelComponent
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Based on RxPM
 * https://github.com/dmdevgo/RxPM
 */

abstract class ReactiveViewModel : ViewModel(), RvmViewModelComponent {

    private val rvmAutoDisposableStore by lazy { DefaultRvmDisposableStore() }

    final override fun Disposable.autoDispose(
        tagKey: String,
        storeKey: RvmAutoDisposableSupport.StoreKey?
    ) = rvmAutoDisposableStore.run { this@autoDispose.autoDispose(tagKey, storeKey) }

    @CallSuper
    override fun onCleared() {
        rvmAutoDisposableStore.dispose()
        super.onCleared()
    }

}
