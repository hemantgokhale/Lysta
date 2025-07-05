package dev.hgokhale.lysta

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.hgokhale.lysta.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Lysta",
    ) {
        App()
    }
}