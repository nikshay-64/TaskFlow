package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.data.PreferencesManager
import com.example.ui.TaskFlowViewModel
import com.example.ui.navigation.TaskFlowApp
import com.example.ui.theme.TaskFlowTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val taskFlowViewModel: TaskFlowViewModel by viewModels {
        TaskFlowViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(applicationContext)

        setContent {
            TaskFlowTheme(darkTheme = true) {
                val isOnboardingCompleted by prefs.onboardingCompleted.collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    if (isOnboardingCompleted != null) {
                        TaskFlowApp(
                            viewModel = taskFlowViewModel,
                            startDestination = if (isOnboardingCompleted == true) "home" else "onboarding",
                            onOnboardingFinished = { name ->
                                coroutineScope.launch {
                                    prefs.setOnboardingCompleted(name)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
