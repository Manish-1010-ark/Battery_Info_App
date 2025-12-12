package com.example.battery.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.ui.theme.AppColors
import com.example.battery.ui.viewmodel.MainViewModel

@Composable
fun DetailsScreen(
    viewModel: MainViewModel,
    scrollController: com.example.battery.util.ScrollVisibilityController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Navigation bar insets
    val navigationBarHeight = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp,
                bottom = navigationBarHeight + 140.dp // Extra space for fixed navbar
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with title and battery percentage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Battery Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = MaterialTheme.shapes.small,
                color = AppColors.getStateColor(
                    uiState.batteryData.batteryPercentage,
                    uiState.batteryData.isCharging,
                    uiState.batteryData.chargingPower
                ).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${uiState.batteryData.batteryPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.getStateColor(
                        uiState.batteryData.batteryPercentage,
                        uiState.batteryData.isCharging,
                        uiState.batteryData.chargingPower
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        DetailsHeader(batteryData = uiState.batteryData)

        PrimaryStatsSection(batteryData = uiState.batteryData)

        DeviceInfoSection(batteryData = uiState.batteryData)
    }
}