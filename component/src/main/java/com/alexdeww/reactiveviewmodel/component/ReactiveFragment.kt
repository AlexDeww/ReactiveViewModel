package com.alexdeww.reactiveviewmodel.component

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import io.reactivex.disposables.Disposable

abstract class ReactiveFragment : Fragment(), RvmAndroidComponent {

    private var _viewLifecycleOwner: InternalLifecycleOwner? = null
    private val disposableOnDestroyList = HashMap<String, Disposable>()
    private val disposableOnStopList = HashMap<String, Disposable>()
    private val disposableOnDestroyViewList = HashMap<String, Disposable>()

    override val viewLifecycleOwner: LifecycleOwner
        get() = _viewLifecycleOwner
            ?: throw IllegalStateException("Can't access the Fragment View's LifecycleOwner when getView() is null i.e., before onCreateView() or after onDestroyView()")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _viewLifecycleOwner = InternalLifecycleOwner()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        super.onPause()
    }

    override fun onStop() {
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        disposableOnStopList.values.forEach { it.dispose() }
        disposableOnStopList.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        if (view != null) _viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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

    private class InternalLifecycleOwner : LifecycleOwner {
        private val viewLifecycleRegistry = LifecycleRegistry(this)
        override fun getLifecycle(): LifecycleRegistry = viewLifecycleRegistry
    }

}