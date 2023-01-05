package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.alexdeww.reactiveviewmodel.widget.RvmWidgetBindShortcut
import io.reactivex.rxjava3.disposables.Disposable

abstract class ReactiveFragment(
    @LayoutRes layoutId: Int = 0
) : Fragment(layoutId), RvmViewComponent, RvmWidgetBindShortcut {

    private val rvmAutoDisposableStore by lazy { DefaultRvmDisposableStore() }
    override val componentLifecycleOwner: LifecycleOwner get() = viewLifecycleOwner

    final override fun Disposable.autoDispose(
        tagKey: String,
        storeKey: RvmAutoDisposableSupport.StoreKey?
    ) = rvmAutoDisposableStore.run { this@autoDispose.autoDispose(tagKey, storeKey) }

    @CallSuper
    override fun onStop() {
        rvmAutoDisposableStore.dispose(RvmViewComponent.onStopStoreKey)
        super.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        rvmAutoDisposableStore.dispose(RvmViewComponent.onDestroyViewStoreKey)
        super.onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        rvmAutoDisposableStore.dispose()
        super.onDestroy()
    }

}
