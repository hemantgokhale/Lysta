package dev.hgokhale.lists

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.hgokhale.lists.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Lysta",
    ) {
        App()
    }
}