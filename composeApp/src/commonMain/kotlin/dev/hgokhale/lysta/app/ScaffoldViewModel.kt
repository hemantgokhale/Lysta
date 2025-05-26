package dev.hgokhale.lysta.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.DrawableResource

data class TopBarAction(
    val contentDescription: String,
    val icon: DrawableResource,
    val isOn: Boolean = false,
    val onClick: () -> Unit,
)

class ScaffoldViewModel : ViewModel() {
    // TopBar attributes
    val topBarTitle = MutableStateFlow("")
    val onTitleChange: MutableStateFlow<((String) -> Unit)?> = MutableStateFlow(null)
    val showBackButton: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val topBarActions: MutableStateFlow<List<TopBarAction>> = MutableStateFlow(emptyList())

    val fabAction: MutableStateFlow<(() -> Unit)?> = MutableStateFlow(null)
}