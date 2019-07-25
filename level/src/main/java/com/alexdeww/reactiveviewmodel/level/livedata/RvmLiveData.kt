package com.alexdeww.reactiveviewmodel.level.livedata

import com.alexdeww.reactiveviewmodel.level.ApiLiveData

abstract class RvmLiveData<T> : ApiLiveData<T>() {

    val hasValue: Boolean get() = value != null

    val valueNonNull: T get() = value!!

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()

    fun getValueOrDef(defValue: T): T = value ?: defValue

}
