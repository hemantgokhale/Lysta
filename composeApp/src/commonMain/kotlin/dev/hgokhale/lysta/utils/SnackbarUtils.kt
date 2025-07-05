package dev.hgokhale.lysta.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class SnackbarEvent(
    val message: String,
    val actionLabel: String,
    val action: (() -> Unit)? = null,
)

interface SnackbarState {
    val snackbarEvents: SharedFlow<SnackbarEvent>
    suspend fun showSnackbar(message: String, actionLabel: String, action: (() -> Unit)? = null)
}

class SnackbarStateImpl : SnackbarState {
    private val _snackbarEvents = MutableSharedFlow<SnackbarEvent>()
    override val snackbarEvents = _snackbarEvents.asSharedFlow()

    override suspend fun showSnackbar(message: String, actionLabel: String, action: (() -> Unit)?) {
        _snackbarEvents.emit(SnackbarEvent(message, actionLabel, action))
    }
}

@Composable
fun ConfigureSnackbar(snackbarState: SnackbarState, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(Unit) {
        snackbarState.snackbarEvents.collect { event ->
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    event.action?.let { it() }
                }
            }
        }
    }
}
