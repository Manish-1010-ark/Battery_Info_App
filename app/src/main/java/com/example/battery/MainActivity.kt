package com.example.battery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.battery.service.BatteryMonitorService
import com.example.battery.ui.navigation.AppBottomNavBar
import com.example.battery.ui.screens.analytics.AnalyticsScreen
import com.example.battery.ui.screens.analytics.viewmodel.AnalyticsViewModel
import com.example.battery.ui.screens.ConfigScreen
import com.example.battery.ui.screens.DashboardScreen
import com.example.battery.ui.screens.SettingsScreen
import com.example.battery.ui.screens.detail.DetailsScreen
import com.example.battery.ui.theme.BatteryTheme
import com.example.battery.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import com.example.battery.util.ScrollVisibilityController
import com.example.battery.util.rememberScrollVisibilityController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val analyticsViewModel: AnalyticsViewModel by viewModels()
    private var hasRequestedPermission = false
    private var isServiceRunning = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "🔔 Notification permission result: $isGranted")
        if (isGranted) startBatteryMonitorService() else startBatteryMonitorService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // edge-to-edge; we'll manage insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // start service / permissions (keep your existing logic)
        requestNotificationPermissionAndStartService()

        setContent {
            BatteryTheme {
                AppContent(mainViewModel = viewModel, analyticsViewModel = analyticsViewModel)
            }
        }
    }

    private fun requestNotificationPermissionAndStartService() {
        if (hasRequestedPermission) return
        hasRequestedPermission = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> startBatteryMonitorService()
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else startBatteryMonitorService()
    }

    private fun startBatteryMonitorService() {
        if (isServiceRunning) return
        try {
            val intent = Intent(this, BatteryMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
            isServiceRunning = true
        } catch (e: Exception) {
            isServiceRunning = false
            Log.e("MainActivity", "Failed to start service", e)
        }
    }
}

/* -------------------------
   AppContent / MainAppContent
   ------------------------- */

@Composable
fun AppContent(mainViewModel: MainViewModel, analyticsViewModel: AnalyticsViewModel) {
    val uiState by mainViewModel.uiState.collectAsState()

    when {
        !mainViewModel.isConfigLoaded -> LoadingSplash()
        !uiState.isConfigured -> ConfigScreen(viewModel = mainViewModel, onConfigComplete = {})
        else -> MainAppContent(viewModel = mainViewModel, analyticsViewModel = analyticsViewModel)
    }
}

@Composable
fun LoadingSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
}

/**
 * Final corrected MainAppContent:
 * - only status bar top padding (no bottom/system padding)
 * - NavHost fills the whole screen so content can go under the floating navbar
 * - FloatingBottomNavBar overlaps the system nav area (no extra inset padding)
 */
@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    analyticsViewModel: AnalyticsViewModel
) {
    val navController = rememberNavController()
    val scrollController = rememberScrollVisibilityController()
    val currentRoute = remember { mutableStateOf("dashboard") }

    // measured navigation bar height (if you need it for custom behavior) - but DO NOT use it to
    // add bottom padding; floating pill should overlap instead.
    val navigationBarHeight: Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // MAIN NAVIGATION CONTENT
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        scrollController = scrollController,
                        )
                }

                composable("details") {
                    DetailsScreen(viewModel = viewModel, scrollController = scrollController)
                }

                composable("analytics") {
                    AnalyticsScreen(viewModel = analyticsViewModel, scrollController = scrollController)
                }

                composable("settings") {
                    SettingsScreen(scrollController = scrollController)
                }
            }

            // Floating nav is positioned above the bottom edge, with a small visual gap.
            // IMPORTANT: do not offset it upward using navigationBarHeight — that creates the blank area.
            FloatingBottomNavBar(
                navController = navController,
                scrollController = scrollController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}


/* -------------------------
   NavHost with transitions
   ------------------------- */

//@Composable
//fun NavHostWithTransitions(
//    navController: NavHostController,
//    currentRoute: MutableState<String>,
//    scrollController: ScrollVisibilityController,
//    viewModel: MainViewModel,
//    analyticsViewModel: AnalyticsViewModel,
//    modifier: Modifier = Modifier
//) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val route = navBackStackEntry?.destination?.route ?: "dashboard"
//
//    LaunchedEffect(route) {
//        currentRoute.value = route
//        scrollController.reset()
//    }
//
//    NavHost(
//        navController = navController,
//        startDestination = "dashboard",
//        modifier = modifier,
//        enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.98f,
//            animationSpec = tween(220)
//        ) },
//        exitTransition = { fadeOut(tween(200)) + scaleOut(targetScale = 0.98f,
//            animationSpec = tween(200)
//        ) }
//    ) {
//        composable("dashboard") {
//            // make sure your DashboardScreen still accepts the scrollController
//            DashboardScreen(viewModel = viewModel, scrollController = scrollController)
//        }
//        composable("details") {
//            DetailsScreen(viewModel = viewModel, scrollController = scrollController)
//        }
//        composable("analytics") {
//            AnalyticsScreen(viewModel = analyticsViewModel, scrollController = scrollController)
//        }
//        composable("settings") {
//            SettingsScreen(scrollController = scrollController)
//        }
//    }
//}

/* -------------------------
   Floating nav (correct)
   ------------------------- */

@Composable
fun FloatingBottomNavBar(
    navController: NavHostController,
    scrollController: ScrollVisibilityController,
    modifier: Modifier = Modifier
) {
    val isVisible by scrollController.isVisible.collectAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 120.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .wrapContentWidth()
            .padding(start = 24.dp,
                end = 24.dp
            )
    ) {
        // AppBottomNavBar should be the corrected compact version (no fillMaxWidth)
        AppBottomNavBar(navController = navController)
    }
}
