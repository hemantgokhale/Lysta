package dev.hgokhale.lysta

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        val list = remember {
            Lyst(
                "Groceries",
                listOf(
                    Lyst.Item("Milk", false),
                    Lyst.Item("Eggs", false),
                    Lyst.Item("Bread", true),
                    Lyst.Item("Butter", true),
                    Lyst.Item("Cheese", true),
                    Lyst.Item("Apples", false),
                    Lyst.Item("Oranges", false),
                    Lyst.Item("Bananas", false),
                    Lyst.Item("Blueberries", true),
                    Lyst.Item("Raspberries", true),
                    Lyst.Item("Grapes", false),
                    Lyst.Item("Strawberries", false),
                    Lyst.Item("Blackberries", true),
                    Lyst.Item("Peaches", true),
                    Lyst.Item("Plums", true),
                    Lyst.Item("Pears", true),
                )
            )
        }
        Lyst(list, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}