package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmViewModelComponent

/**
 * Based on RxPM
 * https://github.com/dmdevgo/RxPM
 */

abstract class ReactiveViewModel : ViewModel(), RvmViewModelComponent,
    RvmAutoDisposableStore by DefaultRvmDisposableStore() {

    @CallSuper
    override fun onCleared() {
        dispose()
        super.onCleared()
    }

}
