package dev.hgokhale.lysta.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


@Composable
fun App(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = viewModel { NavigationViewModel() },
) {
    LaunchedEffect(Unit) {
        navigationViewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Navigate -> navController.navigate(event.route)
                is NavigationEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    LystaTheme {
        Column( // We add this colum to center the app content on a large screen e.g. desktop or web
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.inverseSurface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val primaryTextStyle = MaterialTheme.typography.bodyLarge
            val maxWidth = primaryTextStyle.fontSize.value.dp * 40 // enough space to fit ~80 chars
            CompositionLocalProvider(LocalTextStyle provides primaryTextStyle) {
                Column(modifier = Modifier.widthIn(max = maxWidth)) {
                    NavGraph(navController, navigationViewModel)
                }
            }
        }
    }
}
