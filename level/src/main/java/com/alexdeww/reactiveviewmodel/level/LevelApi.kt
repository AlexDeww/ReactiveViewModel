package com.alexdeww.reactiveviewmodel.level

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel

typealias ApiLiveData<T> = LiveData<T>
typealias ApiViewModel = ViewModel
typealias ApiLifecycleOwner = LifecycleOwner
typealias ApiObserver<T> = Observer<T>