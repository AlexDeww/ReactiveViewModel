package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.disposables.Disposable

abstract class ReactiveFragment : Fragment, RvmViewComponent {

    constructor() : super()

    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    private val disposableOnDestroyList = HashMap<String, Disposable>()
    private val disposableOnStopList = HashMap<String, Disposable>()
    private val disposableOnDestroyViewList = HashMap<String, Disposable>()

    override val componentLifecycleOwner: LifecycleOwner
        get() = viewLifecycleOwner

    @CallSuper
    override fun onStop() {
        disposableOnStopList.values.forEach { it.dispose() }
        disposableOnStopList.clear()
        super.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        disposableOnDestroyViewList.values.forEach { it.dispose() }
        disposableOnDestroyViewList.clear()
        super.onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        disposableOnDestroyList.values.forEach { it.dispose() }
        disposableOnDestroyList.clear()
        super.onDestroy()
    }

    override fun Disposable.disposeOnDestroy(tag: String) {
        disposableOnDestroyList.put(tag, this)?.dispose()
    }

    override fun Disposable.disposeOnStop(tag: String) {
        disposableOnStopList.put(tag, this)?.dispose()
    }

    override fun Disposable.disposeOnDestroyView(tag: String) {
        disposableOnDestroyViewList.put(tag, this)?.dispose()
    }

}
