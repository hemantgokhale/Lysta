package dev.hgokhale.lists.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LystaSnackbar(data: SnackbarData) {
    Surface(
        tonalElevation = 0.dp,
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        shape = RoundedCornerShape(4.dp),
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = data.visuals.message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            data.visuals.actionLabel?.let { actionLabel ->
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionLabel,
                    modifier = Modifier.clickable { data.performAction() },
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (data.visuals.withDismissAction) {
                IconButton(onClick = { data.dismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
        }
    }
}