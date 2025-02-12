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
            Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            val list = remember {
                LystaList(
                    "Groceries",
                    listOf(
                        LystaList.Item("Milk", false),
                        LystaList.Item("Eggs", false),
                        LystaList.Item("Bread", true),
                        LystaList.Item("Butter", true),
                    )
                )
            }
            LystaList(list)
        }
    }
}