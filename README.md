# ReactiveViewModel

This is Android reactive MVVM library, fork https://github.com/dmdevgo/RxPM
   
The Status of the lib: 
[![](https://jitpack.io/v/AlexDeww/ReactiveViewModel.svg)](https://jitpack.io/#AlexDeww/ReactiveViewModel)

How to use this lib in your project:
```gradle
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Add to your app module build.gradle
```gradle
dependencies {
        implementation "com.github.AlexDeww:ReactiveViewModel:$last_version"
}
```

### Пример ViewModel
```kotlin
class EnterSmsCodeViewModel(
    val fullPhoneNumber: String,
    private val requestSmsCode: RequestSmsCode,
    private val registerOrSignIn: RegisterOrSignIn
): ReactiveViewModel() {
    val isProgress = state(false)
    
    val inputSmsCode = inputControl()
    
    val actionSendSmsCodeAgain = emptyAction()
    
    val eventError = event<Throwable>()
    val eventDone = emptyEvent()
    val eventShowSmsCode = event<String>()
    
    init {
        inputSmsCode.text
	    .observable
	    .debounce(250, TimeUnit.MILLISECONDS)
	    .filter { it.length == SMS_CODE_LENGTH }
	    .switchMapSingle {
                registerOrSignIn
                    .asSingle(RegisterOrSignIn.Params(fullPhoneNumber, it))
                    .bindProgress(isProgress.consumer)
                    .doOnError(eventError.consumer)
            }
	    .retry()
	    .subscribe { eventDone.call() }
	    .disposeOnCleared()
	    
	actionSendSmsCodeAgain
	    .observable
            .filter { isProgress.value == false }
            .switchMap {
                requestSmsCode
		    .asSingle(fullPhoneNumber)
                    .bindProgress(isProgress.consumer)
		    .doOnError(eventError.consumer)
            }
            .retry()
            .subscribe(eventShowSmsCode.consumer)
            .disposeOnCleared()
    }
}
```

### Связь с View
```kotlin
class EnterSmsCodeFragment : ReactiveFragment() {

    companion object {
        private const val ARG_FULL_PHONE_NUMBER = "EnterSmsCodeFragment.ARG_FULL_PHONE_NUMBER"
	
        fun create(fullPhoneNumber: String): EnterSmsCodeFragment = EnterSmsCodeFragment()
            .args { putString(ARG_FULL_PHONE_NUMBER, fullPhoneNumber) }
    }
    
    private val viewModel by viewModel<EnterSmsCodeViewModel> {  // koin!!!
        parametersOf(arguments?.getString(ARG_FULL_PHONE_NUMBER)!!)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
	viewModel.isProgress.observe { if (it) showProgress() else hideProgress() }
	
	viewModel.eventError.observe { showError(it) }
	viewModel.eventDone.observe { router.newRootScreen(Screens.Main.TripSetupFlowScreen()) }
	
	viewModel.inputSmsCode
            .bindTo(etSmsCode)
            .disposeOnDestroyView("inputSmsCode")
	    
	viewModel.actionSendSmsCodeAgain.bindOnClick(btnSendSmsAgain)
    }
    
}
```

### State
**State** хранит послдение значение и излучает его при подписке. Используется для передачи значения из ViewModel в View

Создание
```kotlin
val isProgress = state<Boolean>(false)
```
Из ViewModel
```kotlin
isProgress.consumer.accept(true)
isProgress.setValue(true) // расширение для isProgress.consumer.accept(true)
isProgress.setValueIfChanged(true) // расширение для isProgress.consumer.accept(true) но с проверкой if (lastValue != newValue)
```
В View
```kotlin
isProgress.observe { value -> }
```

### Action
**Action** ипользуется для передачи событий или параметров из View в ViewModel

Создание
```kotlin
val actionSendSmsCodeAgain = action<Unit>() // or emptyAction() если тип Unit
```
Из ViewModel 
```kotlin
actionSendSmsCodeAgain.consumer.accept(Unit)
actionSendSmsCodeAgain.call() // расширение для actionSendSmsCodeAgain.consumer.accept(Unit)
```
В View
```kotlin
actionSendSmsCodeAgain.bindOnClick(btnSendSmsCode)
btnSendSmsCode.setOnClickListener { actionSendSmsCodeAgain.call() }
```

### Event
**Event** ипользуется для передачи событий или параметров из ViewModel в View. Хранит последнее переданное значение, пока не появится подписчик.

Создание
```kotlin
val eventDone = event<Unit>() // or emptyEvent() если тип Unit
```
Из ViewModel 
```kotlin
eventDone.consumer.accept(Unit)
eventDone.call() // расширение для eventDone.consumer.accept(Unit)
```
В View
```kotlin
eventDone.observe { value -> }
```


## Инфо
Вся либа, надстройка над LiveData. Cвойства(state, event) имею поле **liveData** для возможности совместного использования с **DataBinding**
