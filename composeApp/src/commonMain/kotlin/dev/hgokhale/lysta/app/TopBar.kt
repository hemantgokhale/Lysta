package dev.hgokhale.lysta.app

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.hgokhale.lysta.home.LystaViewModel
import dev.hgokhale.lysta.lyst.Lyst
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.ic_check_box
import lysta.composeapp.generated.resources.ic_sort
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: LystaViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopAppBar(
        title = { TopBarTitle(uiState) },
        navigationIcon = {
            if ((uiState as? LystaViewModel.UIState.Lyst)?.isListReady == true) {
                IconButton(onClick = { viewModel.onBackArrowClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            (uiState as? LystaViewModel.UIState.Lyst)?.let { lystState ->
                lystState.lyst?.let { list ->
                    val showChecked by list.showChecked.collectAsStateWithLifecycle()
                    IconButton(onClick = { viewModel.onShowCheckedClicked() }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_box),
                            contentDescription = if (showChecked) "Show checked items" else "Hide checked items",
                            tint = if (showChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                        )
                    }

                    val isSorted by list.sorted.collectAsStateWithLifecycle()
                    IconButton(onClick = { viewModel.onSortClicked() }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_sort),
                            contentDescription = if (isSorted) "Sorted" else "Not sorted",
                            tint = if (isSorted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun TopBarTitle(uiState: LystaViewModel.UIState) {
    val textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground)

    (uiState as? LystaViewModel.UIState.Lyst)
        ?.let { lystState ->
            lystState.lyst?.let { list ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LystTitle(list, textStyle, Modifier.weight(1f))
                }
            }
        }
        ?: Text(uiState.title, style = textStyle)
}

@Composable
private fun LystTitle(list: Lyst, textStyle: TextStyle, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val name by list.name.collectAsStateWithLifecycle()
    BasicTextField(
        value = name,
        onValueChange = { list.onNameChanged(it) },
        modifier = modifier,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
    )
}