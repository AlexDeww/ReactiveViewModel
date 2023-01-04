package com.alexdeww.reactiveviewmodel.core.utils

import com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RvmPropertyReadOnlyDelegate<P : Any>(
    private val property: P
) : ReadOnlyProperty<RvmPropertiesSupport, P> {
    override fun getValue(thisRef: RvmPropertiesSupport, property: KProperty<*>): P = this.property
}
