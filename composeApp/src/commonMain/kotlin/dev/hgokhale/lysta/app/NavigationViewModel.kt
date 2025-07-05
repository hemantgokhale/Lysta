package dev.hgokhale.lysta.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface NavigationEvent {
    data class Navigate(val route: String) : NavigationEvent
    data object NavigateBack : NavigationEvent
}

class NavigationViewModel : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    suspend fun navigate(route: String) {
        _navigationEvents.emit(NavigationEvent.Navigate(route))
    }

    suspend fun navigateBack() {
        _navigationEvents.emit(NavigationEvent.NavigateBack)
    }
}