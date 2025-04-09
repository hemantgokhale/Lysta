package dev.hgokhale.lysta

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle

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
            lystState.lyst?.let { lyst ->
                BasicTextField(
                    value = lyst.name.value,
                    onValueChange = { lyst.name.value = it },
                    textStyle = textStyle
                )
            }
        }
        ?: Text(uiState.title, style = textStyle)
}