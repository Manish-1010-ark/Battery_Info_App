package com.example.battery.ui.screens.detail

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.data.model.BatteryData
import com.example.battery.ui.theme.AppColors

@Composable
fun DeviceInfoSection(
    batteryData: BatteryData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Battery Information",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // CRITICAL BATTERY METRICS (Highlighted)

            InfoRow(
                label = "Health Status",
                value = batteryData.batteryHealth,
                valueColor = when (batteryData.batteryHealth) {
                    "Good" -> AppColors.HealthGood
                    "Overheating", "Over Voltage" -> AppColors.HealthBad
                    "Cold" -> AppColors.StateCharging
                    else -> MaterialTheme.colorScheme.onSurface
                },
                isImportant = true
            )

            if (batteryData.designCapacityMah > 0) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                InfoRow(
                    label = "Design Capacity",
                    value = "${batteryData.designCapacityMah} mAh",
                    isImportant = true
                )
            }

            // Cycle Count (Android 14+ Only)
            if (Build.VERSION.SDK_INT >= 34 && batteryData.cycleCount > 0) {
                android.util.Log.d("BatteryInfo", "🔄 Cycle Count displayed = ${batteryData.cycleCount}")

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                InfoRow(
                    label = "Cycle Count",
                    value = "${batteryData.cycleCount} cycles",
                    valueColor = when {
                        batteryData.cycleCount < 200 -> AppColors.HealthGood
                        batteryData.cycleCount < 500 -> AppColors.StateCharging
                        else -> AppColors.HealthBad
                    },
                    isImportant = true
                )
            } else {
                android.util.Log.d(
                    "BatteryInfo",
                    "🔄 Cycle Count unavailable (SDK=${Build.VERSION.SDK_INT}, value=${batteryData.cycleCount}) → Hiding section"
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            InfoRow(
                label = "Technology",
                value = batteryData.chemistry
            )

            // DEVICE INFORMATION (Standard)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            InfoRow(
                label = "Device",
                value = "${Build.MANUFACTURER} ${Build.MODEL}"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            InfoRow(
                label = "Android Version",
                value = "Android ${Build.VERSION.RELEASE}"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            InfoRow(
                label = "API Level",
                value = "API ${Build.VERSION.SDK_INT}"
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
    isImportant: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isImportant) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isImportant)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isImportant) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}