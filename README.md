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

    private val eventCancelTimer = eventNone()

    val progressVisibility = state(initValue, PROGRESS_DEBOUNCE_INTERVAL)
    val timerVisibility = state(false)
    val timerValue = state<Long>()
    val blocked = state(false)

    val inputCode = inputControl()

    val eventWrongSmsCode = eventNone()
    val eventGoToTripStart = eventNone()
    val eventGoToAcceptPolicy = eventNone()
    val eventCodeExpired = eventNone()

    val actionSendCodeAgainClick = debouncedActionNone()
    
    init {
        inputCode.value.observable
            .filter { blocked.value == false && progressVisibility.value == false }
            .debounce()
            .filter { it.length == SMS_CODE_LENGTH }
            .doOnNext { analyticsManager.trackEvent(AppAnalyticEvents.ConfirmPhone) }
            .switchMapSingle { register(it) }
            .doOnError { inputCode.actionChangeValue.call("") }
            .retry()
            .subscribe {
                when {
                    it -> eventGoToTripStart.call()
                    else -> eventGoToAcceptPolicy.call()
                }
            }
            .disposeOnCleared()

        actionSendCodeAgainClick.observable
            .filter { blocked.value == false && timerVisibility.value == false && progressVisibility.value == false }
            .switchMapSingle { requestCode() }
            .switchMap { getTimerObservable(it) }
            .retry()
            .subscribe()
            .disposeOnCleared()

        getRequestSmsTimerValue.execute(Unit)
            .takeIf { it > 0 }
            ?.let { getTimerObservable(it) }
            ?.subscribe()
            ?.disposeOnCleared()
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
        
	viewModel.progressVisibility.observe { if (it) showProgress() else hideProgress() }
	viewModel.timerValue.observe { tvTimer.text = formatTimer(it) }
	viewModel.timerVisibility.observe { tvTimer.isVisible = it }
	
	viewModel.eventError.observe { showError(it) }
	viewModel.eventGoToTripStart.observe { router.newRootScreen(Screens.Main.TripSetupFlowScreen()) }
	
	viewModel.inputCode.bindTo(etSmsCode)
        viewModel.actionSendCodeAgainClick.bindOnClick(btnSendAgain)
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
val actionSendSmsCodeAgain = action<Unit>() // or actionEmpty() если тип Unit
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
val eventDone = event<Unit>() // or eventEmpty() если тип Unit
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
