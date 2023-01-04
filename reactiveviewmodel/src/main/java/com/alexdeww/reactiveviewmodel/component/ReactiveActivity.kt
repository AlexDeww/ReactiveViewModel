package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.disposables.Disposable

abstract class ReactiveActivity : AppCompatActivity(), RvmViewComponent,
    RvmAutoDisposableStore by DefaultRvmDisposableStore() {

    override val componentLifecycleOwner: LifecycleOwner get() = this

    @CallSuper
    override fun onStop() {
        dispose(RvmViewComponent.onStopStoreKey)
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

    override fun Disposable.disposeOnDestroyView(tag: String) {
        autoDispose("dv-$tag", RvmViewComponent.onDestroyViewStoreKey)
    }

}
