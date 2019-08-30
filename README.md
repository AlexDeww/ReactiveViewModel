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
                requestSmsCode.asSingle(fullPhoneNumber)
                    .bindProgress(isProgress.consumer)
		    .doOnError(eventError.consumer)
            }
            .retry()
            .subscribe(eventShowSmsCode.consumer)
            .disposeOnCleared()
    }
}
```
