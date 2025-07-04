package dev.hgokhale.lists.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.DrawableResource

data class TopBarAction(
    val contentDescription: String,
    val icon: DrawableResource,
    val isOn: Boolean = false,
    val onClick: () -> Unit,
)

sealed interface UiEvent {
    sealed interface Navigation : UiEvent {
        data class Navigate(val route: String) : Navigation
        data object NavigateBack : Navigation
    }

    data class Snackbar(
        val message: String,
        val actionLabel: String,
        val action: (() -> Unit)? = null
    ) : UiEvent
}

data class TopBarState(
    val actions: List<TopBarAction> = emptyList(),
    val onTitleChange: ((String) -> Unit)? = null,
    val focusOnTitle: Boolean = false,
    val showBackButton: Boolean = false,
    val title: String = "",
)

class ScaffoldViewModel : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState = _topBarState.asStateFlow()

    private val _fabAction = MutableStateFlow<(() -> Unit)?>(null)
    val fabAction = _fabAction.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun focusOnTitle(requestFocus: Boolean) {
        _topBarState.update { it.copy(focusOnTitle = requestFocus) }
    }

    fun updateTopBarTitle(title: String) {
        _topBarState.update { it.copy(title = title) }
    }

    fun setTopBarActions(actions: List<TopBarAction>) {
        _topBarState.update { it.copy(actions = actions) }
    }

    fun setOnTitleChange(onTitleChange: ((String) -> Unit)?) {
        _topBarState.update { it.copy(onTitleChange = onTitleChange) }
    }

    fun setFabAction(action: (() -> Unit)?) {
        _fabAction.update { action }
    }

    fun showBackButton(show: Boolean) {
        _topBarState.update { it.copy(showBackButton = show) }
    }

    suspend fun showSnackbar(message: String, actionLabel: String, action: (() -> Unit)? = null) {
        _uiEvents.emit(UiEvent.Snackbar(message, actionLabel, action))
    }

    suspend fun navigate(route: String) {
        _uiEvents.emit(UiEvent.Navigation.Navigate(route))
    }

    suspend fun navigateBack() {
        _uiEvents.emit(UiEvent.Navigation.NavigateBack)
    }
}