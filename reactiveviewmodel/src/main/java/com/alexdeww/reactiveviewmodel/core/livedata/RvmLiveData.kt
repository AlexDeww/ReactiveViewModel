package com.alexdeww.reactiveviewmodel.core.livedata

import androidx.lifecycle.LiveData

abstract class RvmLiveData<T> : LiveData<T>() {

    val hasValue: Boolean get() = value != null

    val valueNonNull: T get() = value!!

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()

    fun getValueOrDef(defValue: T): T = value ?: defValue

}
