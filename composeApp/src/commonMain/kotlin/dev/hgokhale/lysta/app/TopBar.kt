package dev.hgokhale.lysta.app

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(scaffoldViewModel: ScaffoldViewModel) {
    val topBarState by scaffoldViewModel.topBarState.collectAsStateWithLifecycle()

    TopAppBar(
        title = { TopBarTitle(topBarState = topBarState) },
        navigationIcon = {
            val scope = rememberCoroutineScope()
            if (topBarState.showBackButton) {
                IconButton(onClick = { scope.launch { scaffoldViewModel.navigateBack() } }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            topBarState.actions.forEach { action ->
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
private fun TopBarTitle(topBarState: TopBarState) {
    val textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground)
    val title = topBarState.title
    val onTitleChange = topBarState.onTitleChange

    onTitleChange
        ?.let { LystTitle(title = title, onTitleChange = it, textStyle = textStyle, focusOnTitle = topBarState.focusOnTitle) }
        ?: Text(title, style = textStyle)
}

@Composable
private fun LystTitle(title: String, onTitleChange: (String) -> Unit, textStyle: TextStyle, focusOnTitle: Boolean, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val textFieldState = rememberTextFieldState(initialText = title)

    LaunchedEffect(Unit) {
        if (focusOnTitle) {
            focusRequester.requestFocus()
            textFieldState.setTextAndSelectAll(title)
        }
        snapshotFlow { textFieldState.text.toString() }.collect { onTitleChange(it) }
    }

    BasicTextField(
        state = textFieldState,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        onKeyboardAction = { defaultAction -> focusManager.clearFocus(); defaultAction() },
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
    )
}