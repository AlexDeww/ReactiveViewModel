# ReactiveViewModel

It's a fork https://github.com/dmdevgo/RxPM

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
    
    private val viewModel by viewModel<EnterSmsCodeViewModel> { 
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
**State** хранит послдение значение и излучает его при подписке.

Создание
```kotlin
val isProgress = state<Boolean>(false)
```
Изменения значения
```kotlin
isProgress.consumer.accept(true)
isProgress.setValue(true) // расширение для isProgress.consumer.accept(true)
isProgress.setValueIfChanged(true) // расширение для isProgress.consumer.accept(true) но с проверкой if (lastValue != newValue)
```
