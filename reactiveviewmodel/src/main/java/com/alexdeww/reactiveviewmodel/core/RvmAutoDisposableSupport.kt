package com.alexdeww.reactiveviewmodel.core

import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

interface RvmAutoDisposableSupport {

    data class StoreKey(val name: String) {
        init {
            check(name.isNotBlank()) { "Name can`t be blank" }
        }
    }

    fun Disposable.autoDispose(
        tagKey: String = UUID.randomUUID().toString(),
        storeKey: StoreKey? = null
    )

}

interface RvmAutoDisposableStore : RvmAutoDisposableSupport {
    fun dispose(storeKey: RvmAutoDisposableSupport.StoreKey? = null)
}

class DefaultRvmDisposableStore : RvmAutoDisposableStore {

    private val disposablesStore =
        hashMapOf<RvmAutoDisposableSupport.StoreKey?, HashMap<String, Disposable>>()

    override fun dispose(storeKey: RvmAutoDisposableSupport.StoreKey?) {
        if (storeKey == null) {
            disposablesStore.entries.forEach { it.value.disposeAndClear() }
            disposablesStore.clear()
        } else {
            disposablesStore[storeKey]?.disposeAndClear()
        }
    }

    override fun Disposable.autoDispose(tagKey: String, storeKey: RvmAutoDisposableSupport.StoreKey?) {
        disposablesStore.getOrPut(storeKey) { hashMapOf() }.put(tagKey, this)?.dispose()
    }

    private fun HashMap<String, Disposable>.disposeAndClear() {
        values.forEach { it.dispose() }
        clear()
    }

}
