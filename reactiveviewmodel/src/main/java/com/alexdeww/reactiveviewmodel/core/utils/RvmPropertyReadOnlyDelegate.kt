package com.alexdeww.reactiveviewmodel.core.utils

import com.alexdeww.reactiveviewmodel.core.RvmComponentsSupport
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RvmPropertyReadOnlyDelegate<P : Any>(
    private val property: P
) : ReadOnlyProperty<RvmComponentsSupport, P> {
    override fun getValue(thisRef: RvmComponentsSupport, property: KProperty<*>): P = this.property
}
