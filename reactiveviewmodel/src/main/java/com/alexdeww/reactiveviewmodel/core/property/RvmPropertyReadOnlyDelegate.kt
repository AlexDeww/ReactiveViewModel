package com.alexdeww.reactiveviewmodel.core.property

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RvmPropertyReadOnlyDelegate<P : Any>(
    private val property: P
) : ReadOnlyProperty<Any?, P> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): P = this.property
}
