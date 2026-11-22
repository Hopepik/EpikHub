package com.epikhub.epikhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.epikhub.epikhub.ui.navigation.MainAppScaffold
import com.epikhub.epikhub.ui.theme.EpikHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpikHubTheme {
                MainAppScaffold()
            }
        }
    }
}
