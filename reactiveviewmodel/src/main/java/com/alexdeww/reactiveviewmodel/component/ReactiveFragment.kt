package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.DefaultRvmDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmAutoDisposableStore
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent

abstract class ReactiveFragment(
    @LayoutRes layoutId: Int = 0
) : Fragment(layoutId), RvmViewComponent,
    RvmAutoDisposableStore by DefaultRvmDisposableStore() {

    override val componentLifecycleOwner: LifecycleOwner get() = viewLifecycleOwner

    @CallSuper
    override fun onStop() {
        dispose(RvmViewComponent.onStopStoreKey)
        super.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        dispose(RvmViewComponent.onDestroyViewStoreKey)
        super.onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

}
