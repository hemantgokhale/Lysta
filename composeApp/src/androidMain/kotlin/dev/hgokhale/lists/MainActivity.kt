package dev.hgokhale.lists

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import dev.hgokhale.lists.db.initSharedModule
import dev.hgokhale.lists.app.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initSharedModule(context = this)
        setContent {
            App()
            SetSystemBarColor(darkTheme = isSystemInDarkTheme())
        }
    }
}

@Composable
fun SetSystemBarColor(darkTheme: Boolean) {
    val window = (LocalContext.current as? Activity)?.window
        ?: return
    SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}