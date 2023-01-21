package com.alexdeww.reactiveviewmodel.core.property

import androidx.lifecycle.LiveData
import com.alexdeww.reactiveviewmodel.core.livedata.LiveEvent
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * В основном предназначен для передачи событий или данных из ViewModel во View.
 *
 * * Передавать данные в [RvmEvent] могут только наследники
 * [RvmPropertiesSupport][com.alexdeww.reactiveviewmodel.core.RvmPropertiesSupport].
 *
 * * Хранит последнее переданное значение пока не появится подписчик.
 * Только первый подписчик получит последнее сохраненное значение,
 * все последующие подписки, будут получать только новые значения.
 *
 * * Пока есть активная подписка, данные не сохраняются.
 */
class RvmEvent<T : Any> internal constructor(
    debounceInterval: Long? = null
) : RvmProperty<T>(), RvmCallableProperty<T> {

    private val subject = BehaviorSubject.create<T>()
    private val serializedSubject = subject.toSerialized()
    private val isPending = AtomicBoolean(false)

    override val consumer: Consumer<T> = Consumer {
        isPending.set(true)
        serializedSubject.onNext(it)
    }

    override val observable: Observable<T> = Observable
        .create<T> { emitter ->
            val skipCount = if (!isPending.get() && subject.hasValue()) 1L else 0L
            val d = serializedSubject.skip(skipCount).subscribe {
                isPending.set(false)
                emitter.onNext(it)
            }
            emitter.setCancellable { d.dispose() }
        }
        .letDebounce(debounceInterval)
        .share()

    override val liveData: LiveData<T> by lazy { EventLiveData() }
    override val viewFlowable: Flowable<T> by lazy { observable.toViewFlowable() }

    private inner class EventLiveData : LiveEvent<T>() {

        private var disposable: Disposable? = null

        override fun onActive() {
            super.onActive()
            disposable = viewFlowable.subscribe { value = it }
        }

        override fun onInactive() {
            disposable?.dispose()
            super.onInactive()
        }

    }

}
