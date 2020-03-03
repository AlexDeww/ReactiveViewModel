package com.alexdeww.reactiveviewmodel.component

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.disposables.Disposable

abstract class ReactiveFragment : Fragment(), RvmViewComponent {

    private val disposableOnDestroyList = HashMap<String, Disposable>()
    private val disposableOnStopList = HashMap<String, Disposable>()
    private val disposableOnDestroyViewList = HashMap<String, Disposable>()

    override val componentLifecycleOwner: LifecycleOwner
        get() = viewLifecycleOwner

    override fun onStop() {
        disposableOnStopList.values.forEach { it.dispose() }
        disposableOnStopList.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        disposableOnDestroyViewList.values.forEach { it.dispose() }
        disposableOnDestroyViewList.clear()
        super.onDestroyView()
    }

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