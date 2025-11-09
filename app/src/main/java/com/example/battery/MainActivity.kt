package com.example.battery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.battery.service.BatteryMonitorService
import com.example.battery.ui.screens.ConfigScreen
import com.example.battery.ui.screens.MainScreen
import com.example.battery.ui.theme.BatteryTheme
import com.example.battery.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var hasRequestedPermission = false
    private var isServiceRunning = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startBatteryMonitorService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BatteryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    // Request permission and start service ONLY when configured
                    LaunchedEffect(uiState.isConfigured) {
                        if (uiState.isConfigured && !isServiceRunning) {
                            requestNotificationPermissionAndStartService()
                        }
                    }

                    // Simple routing based on configuration state
                    if (uiState.isConfigured) {
                        // Show main screen with both batteryData and graphData
                        MainScreen(
                            batteryData = uiState.batteryData,
                            graphData = uiState.graphData
                        )
                    } else {
                        // Show config screen
                        ConfigScreen(
                            viewModel = viewModel,
                            onConfigComplete = {
                                // Configuration saved - uiState.isConfigured will automatically update
                                // LaunchedEffect above will trigger service start
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Request notification permission (Android 13+) and start service
     */
    private fun requestNotificationPermissionAndStartService() {
        if (hasRequestedPermission) return
        hasRequestedPermission = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startBatteryMonitorService()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startBatteryMonitorService()
        }
    }

    /**
     * Start the foreground battery monitoring service
     */
    private fun startBatteryMonitorService() {
        if (isServiceRunning) return

        try {
            val intent = Intent(this, BatteryMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        } catch (e: Exception) {
            // Service start failed - could be permission issue or Android 14+ restriction
            android.util.Log.e("MainActivity", "Failed to start service", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Note: We DON'T stop the service here - it should continue running
        // The service will stop itself when needed or when user manually stops it
    }
}