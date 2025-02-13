package dev.hgokhale.lysta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            val list = remember {
                Lyst(
                    "Groceries",
                    listOf(
                        Lyst.Item("Milk", false),
                        Lyst.Item("Eggs", false),
                        Lyst.Item("Bread", true),
                        Lyst.Item("Butter", true),
                    )
                )
            }
            Lyst(list)
        }
    }
}