# ReactiveViewModel

Очередная библиотека Android Reactive MVVM, за основу взята https://github.com/dmdevgo/RxPM
 
Основная цель библиотеки, это организовать направление данных **View** <-> **ViewModel** и **сокрытия** методов по изменению состояния напрямую из **View**. 
 
Пример такого сокрытия вы могли видеть (или применять) при использовании **LiveData** 

```kotlin
prvate val _state = MutableLiveData<DataType>()
val state: LiveData<DataType> = _state 
```

Вместо этого, библиотека предлагает делать это [так](#пример-viewmodel)
   
[![](https://jitpack.io/v/AlexDeww/ReactiveViewModel.svg)](https://jitpack.io/#AlexDeww/ReactiveViewModel)

Подключение:
```gradle
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

```gradle
dependencies {
        implementation "com.github.AlexDeww:ReactiveViewModel:$last_version"
}
```

### Пример ViewModel
```kotlin
class SomeViewModel(
    savedState: SavedStateHandle
) : ReactiveViewModel() {

    val progressVisible by RVM.progressState(initialValue = false)
    val tryCount by savedState.state(initialValue = 5)
    
    val tryCountLabelVisible by RVM.stateProjection(tryCount) { it > 0 }
    val sendButtonEnable by RVM.stateProjectionFromSource(initialValue = false) {
        combineLatest(
            progressVisible.observable,
            tryCountLabelVisible.observable,
            inputCode.data.observable.map { it.length >= 4 }
        ) { isProgress, hasTryCount, codeReached -> !isProgress && hasTryCount && codeReached }
    }

    val inputCode by savedState.inputControl()

    val eventDone by RVM.eventNone()
    val eventError by RVM.event<Throwable>()

    val actionOnSendCodeClick by RVM.debouncedActionNone()

    private val sendCode by RVM.invocable<String> { code ->
        Completable
            .fromAction {
                tryCount.valueNonNull.takeIf { it > 0 }?.let { tryCount.setValue(it - 1) }
                Log.d("VM", "Code sent: $code")
            }
            .delaySubscription(5, TimeUnit.SECONDS)
            .bindProgress(progressVisible.consumer)
            .doOnComplete { eventDone.call() }
            .doOnError { eventError.call(it) }
    }

    init {
        actionOnSendCodeClick.bind {
            this.filter { sendButtonEnable.value == true }
                .doOnNext { sendCode(inputCode.data.valueNonNull) }
        }
    }

}
```

### Связь с View
```kotlin
class SomeFragment : ReactiveFragment() {

    private val viewModel: SomeViewModel by Delegates.notNull()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.progressVisible.observe { progressView.isVisible = it }
        viewModel.tryCount.observe { tryCountLabel.text = it.toString() }
        viewModel.tryCountLabelVisible.observe { tryCountLabel.isVisible = it }
        viewModel.sendButtonEnable.observe { sendButton.isEnabled = it }

        viewModel.inputCode.bindTo(codeEditText)

        viewModel.eventDone.observe { /* close fragment */ }
        viewModel.eventError.observe { /* show error */ }

        viewModel.actionOnSendCodeClick.bindOnClick(sendButton)
    }

}
```

## Есть 5 базовых объектов для взаимодействия View и ViewModel

### RvmState
В основном предназначен для передачи состояния из **ViewModel** во **View**.
 - Передавать данные в **RvmState** могут только наследники **RvmPropertiesSupport**.
 - Всегда хранит последнее переданное значение.
 - Каждый подписчик в первую очередь получит последннее сохраненное значение.
 
Объявление:

```kotlin
val state by RVM.state<DataType>(initialValue = null or data)
```
```kotlin
val state by savedStateHandle.state<DataType>(initialValue = null or data)
```

Подписка:

```kotlin
viewModel.state.observe { value -> /* do */ }
```


### RvmStateProjection
Почти тоже самое, что и **RvmState**, но отличается тем, что никто не может передавать данные напрямую.
 - Никто не может передавать данные напрямую. **RvmStateProjection** может получать данные от источников: **Observable**, **RvmState**, **RvmStateProjection**, либо объекта наследника **RvmPropertyBase** и **RvmValueProperty**.

Объявление:

```kotlin
val state by RVM.state<DataType>(initialValue = null or data)
val stateProjection by RVM.stateProjection(state) { /* map block */ }
```
```kotlin
val stateProjection by RVM.stateProjectionFromSource(initialValue = null or data) { ObservableSource }
```

Подписка:

```kotlin
viewModel.stateProjection.observe { value -> /* do */ }
```

### RvmEvent
В основном предназначен для передачи событий или данных из **ViewModel** во **View**.
 - Передавать данные в **RvmEvent** могут только наследники **RvmPropertiesSupport**.
 - Хранит последнее переданное значение пока не появится подписчик. Только первый подписчик получит последнее сохраненное значение, все последующие подписки, будут получать только новые значения.
 - Пока есть активная подписка, данные не сохраняются.

Объявление:

```kotlin
val event by RVM.event<DataType>()
```
```kotlin
val event by RVM.eventNone() // for Unit Data Type 
```

Подписка:

```kotlin
viewModel.event.observe { value -> /* do */ }
```

### RvmConfirmationEvent
Почти тоже самое, что и **RvmEvent**, но отличается тем, что хранит последнее значение пока не будет вызван метод **confirm**.
 - Передавать данные в **RvmConfirmationEvent** могут только наследники **RvmPropertiesSupport**.
 - Хранит последнее переданное значение пока не будет вызван метод **confirm**. Каждый новый подписчик будет получать последнее сохраненное значение, пока не вызван метод **confirm**.

Объявление:

```kotlin
val confirmationEvent by RVM.confirmationEvent<DataType>()
```
```kotlin
val confirmationEvent by RVM.confirmationEventNone() // for Unit Data Type 
```

Подписка:

```kotlin
viewModel.confirmationEvent.observe { value -> 
    /* do */
    viewModel.confirmationEvent.confirm()
}
```

### RvmAction
В основном предназначен для передачи событий или данных из **View** во **ViewModel**.
 - Не хранит данные.

Объявление:

```kotlin
val action by RVM.action<DataType>()
```
```kotlin
val action by RVM.actionNone() // for Unit Data Type 
```

Привязка:

```kotlin
viewModel.action.bindOnClick(someView)
```

## Также имеется 4 вспомогательных объекта для удобной связи View-элементов с ViewModel

### RvmCheckControl
Для связи **CompoundButton** и **ViewModel**.

Объявление:

```kotlin
val checkControl by RVM.checkControl(initialChecked = false)
```
```kotlin
val checkControl by savedStateHandle.checkControl(initialChecked = false)
```

Привязка:

```kotlin
viewModel.checkControl.bindTo(compoundButton)
```

### RvmRatingControl
Для связи **RatingBar** и **ViewModel**.

Объявление:

```kotlin
val ratingControl by RVM.ratingControl(initialValue = 3)
```
```kotlin
val ratingControl by savedStateHandle.ratingControl(initialValue = 3)
```

Привязка:

```kotlin
viewModel.ratingControl.bindTo(ratingBar)
```

### RvmInputControl
Для связи **EditText** или **TextInputLayout** и **ViewModel**.

Объявление:

```kotlin
val inputControl by RVM.inputControl(initialText = "Some Text")
```
```kotlin
val inputControl by savedStateHandle.inputControl(initialText = "Some Text")
```

Привязка:

```kotlin
viewModel.inputControl.bindTo(editText)
```
```kotlin
viewModel.inputControl.bindTo(textInputLayout)
```

### RvmDialogControl
Для связи **Dialog** и **ViewModel**.

Объявление:

```kotlin
val dialogControl by RVM.dialogControl<DataType, ResultDataType>()
```
```kotlin
val dialogControl by RVM.dialogControlDefaultResult<DataType>()
```

Привязка:

```kotlin
viewModel.dialogControl.bindTo { data: DataType, dc: RvmDialogControlResult<ResultDataType> ->
    MaterialAlertDialogBuilder(context)
        .title("Title")
        .message("Message")
        .setPositiveButton("OK") { _, _ -> dc.sendResult(ResultDataType) }
        .setNegativeButton("Cancel") { _, _ -> dc.sendResult(ResultDataType) }
        .create()
}
```
