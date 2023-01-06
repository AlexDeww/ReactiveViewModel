package com.alexdeww.reactiveviewmodel.core.property

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer

abstract class RvmPropertyBase<T : Any> {
    internal abstract val consumer: Consumer<T>
    internal abstract val observable: Observable<T>
}

@Suppress("UnnecessaryAbstractClass")
abstract class RvmProperty<T : Any> : RvmPropertyBase<T>()

interface RvmObservableProperty<T : Any> {
    val viewFlowable: Flowable<T>
    val liveData: LiveData<T>
}

interface RvmValueProperty<T : Any> : RvmObservableProperty<T> {
    val value: T?
    val valueNonNull: T get() = value!!
    val hasValue: Boolean get() = value != null

    fun getValueOrDef(actionDefValue: () -> T): T = value ?: actionDefValue()
    fun getValueOrDef(defValue: T): T = getValueOrDef { defValue }
}

interface RvmCallableProperty<T : Any> : RvmObservableProperty<T>

interface RvmMutableValueProperty<T : Any> : RvmValueProperty<T>
