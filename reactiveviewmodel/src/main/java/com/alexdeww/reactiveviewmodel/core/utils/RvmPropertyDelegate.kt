package com.alexdeww.reactiveviewmodel.core.utils

import com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RvmPropertyDelegate<R : RvmPropertiesSupport, P : Any> private constructor(
    private val initializer: R.(property: KProperty<*>) -> P
) : ReadOnlyProperty<R, P> {

    companion object {
        fun <R : RvmPropertiesSupport, P : Any> def(
            initializer: R.(property: KProperty<*>) -> P
        ): ReadOnlyProperty<R, P> = RvmPropertyDelegate(initializer)
    }

    private var value: P? = null

    override fun getValue(thisRef: R, property: KProperty<*>): P {
        if (value == null) value = initializer(thisRef, property)
        return value!!
    }

}
