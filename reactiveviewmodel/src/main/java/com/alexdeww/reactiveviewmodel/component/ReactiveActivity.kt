package com.alexdeww.reactiveviewmodel.component

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import io.reactivex.rxjava3.disposables.Disposable

abstract class ReactiveActivity : AppCompatActivity(), RvmViewComponent {

    private val disposableOnDestroyList = HashMap<String, Disposable>()
    private val disposableOnStopList = HashMap<String, Disposable>()

    override val componentLifecycleOwner: LifecycleOwner
        get() = this

    @CallSuper
    override fun onStop() {
        disposableOnStopList.values.forEach { it.dispose() }
        disposableOnStopList.clear()
        super.onStop()
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
        disposableOnDestroyList.put("dv-$tag", this)?.dispose()
    }

}