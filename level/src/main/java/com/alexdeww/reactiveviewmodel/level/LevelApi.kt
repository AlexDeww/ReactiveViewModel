package com.alexdeww.reactiveviewmodel.level

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

typealias ApiLiveData<T> = LiveData<T>
typealias ApiViewModel = ViewModel
typealias ApiLifecycleOwner = LifecycleOwner
typealias ApiObserver<T> = Observer<T>