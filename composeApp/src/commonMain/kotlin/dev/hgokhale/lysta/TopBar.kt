package dev.hgokhale.lysta

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, viewModel: LystViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    TopAppBar(
        title = { Text(uiState.title) },
        navigationIcon = {
            if (uiState !is LystViewModel.UIState.Home) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}