package org.eos.mynoti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.eos.mynoti.ui.navigation.MyNotiApp
import org.eos.mynoti.ui.theme.MyNotiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MyNotiApplication).container
        setContent {
            MyNotiTheme {
                MyNotiApp(container = container)
            }
        }
    }
}
