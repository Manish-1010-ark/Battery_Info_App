package com.example.battery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.battery.ui.screens.dashboard.DashboardRoot
import com.example.battery.ui.viewmodel.MainViewModel

/**
 * Dashboard Screen Entry Point
 *
 * Integrates with scroll-aware navigation system
 */
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    scrollController: com.example.battery.util.ScrollVisibilityController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollController.nestedScrollConnection)
    ) {
        DashboardRoot(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()        )
    }
}