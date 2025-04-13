package dev.hgokhale.lysta

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: LystViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    TopAppBar(
        title = { TopBarTitle(uiState) },
        navigationIcon = {
            if ((uiState as? LystViewModel.UIState.Lyst)?.lyst != null) {
                IconButton(onClick = { viewModel.onBackArrowClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}

@Composable
private fun TopBarTitle(uiState: LystViewModel.UIState) {
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = MaterialTheme.typography.titleLarge.fontSize)

    (uiState as? LystViewModel.UIState.Lyst)
        ?.let { lystState ->
            lystState.lyst?.let { list ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LystTitle(list, textStyle, Modifier.weight(1f))
                    SortSelector(list, textStyle)
                }
            }
        }
        ?: Text(uiState.title, style = textStyle)
}

@Composable
private fun LystTitle(list: Lyst, textStyle: TextStyle, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = list.name.value,
        onValueChange = { list.name.value = it },
        modifier = modifier,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true
    )
}

@Composable
private fun SortSelector(list: Lyst, textStyle: TextStyle, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text("Sort ", style = textStyle)
        Switch(
            checked = list.sorted.value,
            onCheckedChange = { list.sorted.value = it },
            modifier = Modifier.scale(0.75f)
        )
    }
}