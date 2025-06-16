package dev.hgokhale.lysta.app

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: ScaffoldViewModel) {

    TopAppBar(
        title = { TopBarTitle(viewModel = viewModel) },
        navigationIcon = {
            val showBackButton by viewModel.showBackButton.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            if (showBackButton) {
                IconButton(onClick = { scope.launch { viewModel.navigationEvents.send(NavigationEvent.NavigateBack) } }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            val actions by viewModel.topBarActions.collectAsStateWithLifecycle()
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        painter = painterResource(action.icon),
                        contentDescription = action.contentDescription,
                        tint = if (action.isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
        }
    )
}

@Composable
private fun TopBarTitle(viewModel: ScaffoldViewModel) {
    val textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground)
    val title by viewModel.topBarTitle.collectAsStateWithLifecycle()
    val onTitleChange by viewModel.onTitleChange.collectAsStateWithLifecycle()

    onTitleChange
        ?.let { LystTitle(title = title, onTitleChange = it, textStyle = textStyle) }
        ?: Text(title, style = textStyle)
}

@Composable
private fun LystTitle(title: String, onTitleChange: (String) -> Unit, textStyle: TextStyle, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
    )
}