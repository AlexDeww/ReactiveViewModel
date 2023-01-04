package com.alexdeww.reactiveviewmodel.widget

import com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import java.lang.ref.WeakReference

@Suppress("UnnecessaryAbstractClass")
abstract class BaseControl<B : BaseControl.ViewBinder> : RvmPropertiesSupport {

    abstract class ViewBinder(rvmViewComponent: RvmViewComponent) {
        protected val rvmViewComponentRef: WeakReference<RvmViewComponent> =
            WeakReference(rvmViewComponent)
    }

    internal abstract fun getBinder(rvmViewComponent: RvmViewComponent): B

}
