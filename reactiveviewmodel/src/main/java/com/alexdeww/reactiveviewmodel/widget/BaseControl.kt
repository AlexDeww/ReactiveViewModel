package com.alexdeww.reactiveviewmodel.widget

import com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport
import com.alexdeww.reactiveviewmodel.core.RvmViewComponent
import com.alexdeww.reactiveviewmodel.core.property.RvmCallableProperty
import com.alexdeww.reactiveviewmodel.core.property.RvmMutableValueProperty
import com.alexdeww.reactiveviewmodel.core.property.RvmProperty
import com.alexdeww.reactiveviewmodel.core.property.RvmPropertyBase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.lang.ref.WeakReference

@Suppress("UnnecessaryAbstractClass")
abstract class BaseControl<B : BaseControl.ViewBinder> : RvmPropertiesSupport {

    private val defaultPropertiesSupport = object : RvmPropertiesSupport {}

    abstract class ViewBinder(rvmViewComponent: RvmViewComponent) {
        protected val rvmViewComponentRef: WeakReference<RvmViewComponent> =
            WeakReference(rvmViewComponent)
    }

    internal abstract fun getBinder(rvmViewComponent: RvmViewComponent): B

    final override val <T : Any> RvmProperty<T>.consumer: Consumer<T>
        get() = defaultPropertiesSupport.run { consumer }
    final override val <T : Any> RvmPropertyBase<T>.observable: Observable<T>
        get() = defaultPropertiesSupport.run { observable }

    final override fun <T : Any, R> R.call(value: T) where R : RvmCallableProperty<T>,
                                                           R : RvmProperty<T> {
        defaultPropertiesSupport.run { call(value) }
    }

    final override fun <R> R.call() where R : RvmCallableProperty<Unit>,
                                          R : RvmProperty<Unit> {
        defaultPropertiesSupport.run { call() }
    }

    final override fun <T : Any, R> R.setValue(value: T) where R : RvmMutableValueProperty<T>,
                                                               R : RvmProperty<T> {
        defaultPropertiesSupport.run { setValue(value) }
    }

    final override fun <T : Any, R> R.setValueIfChanged(value: T) where R : RvmMutableValueProperty<T>,
                                                                        R : RvmProperty<T> {
        defaultPropertiesSupport.run { setValueIfChanged(value) }
    }

}
