package dev.hgokhale.lysta.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.DrawableResource

data class TopBarAction(
    val contentDescription: String,
    val icon: DrawableResource,
    val isOn: Boolean = false,
    val onClick: () -> Unit,
)

sealed interface NavigationEvent {
    data class Navigate(val route: String) : NavigationEvent
    data object NavigateBack : NavigationEvent
}

class ScaffoldViewModel : ViewModel() {
    // TopBar
    val topBarTitle = MutableStateFlow("")
    val onTitleChange: MutableStateFlow<((String) -> Unit)?> = MutableStateFlow(null)
    val showBackButton: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val topBarActions: MutableStateFlow<List<TopBarAction>> = MutableStateFlow(emptyList())

    val navigationEvents = Channel<NavigationEvent>(capacity = Channel.CONFLATED)
    val fabAction: MutableStateFlow<(() -> Unit)?> = MutableStateFlow(null)
}