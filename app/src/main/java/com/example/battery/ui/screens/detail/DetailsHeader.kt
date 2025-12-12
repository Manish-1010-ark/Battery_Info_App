package com.example.battery.ui.screens.detail

import android.R.color.black
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.data.model.BatteryData
import com.example.battery.ui.theme.AppColors

@Composable
fun DetailsHeader(
    batteryData: BatteryData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = batteryData.chargingType,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCell(
                        value = "%.1f".format(batteryData.chargingPower),
                        unit = "W",
                        label = "Power",
                        color = AppColors.AccentMint,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCell(
                        value = "%.2f".format(batteryData.voltage),
                        unit = "V",
                        label = "Voltage",
                        color = AppColors.AccentMint,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCell(
                        value = "%.2f".format(batteryData.currentAmps),
                        unit = "A",
                        label = "Current",
                        color = if (batteryData.isCharging) AppColors.StateCharging else AppColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (batteryData.isCharging && batteryData.timeRemainingCharging != null && batteryData.timeRemainingState != "FULL") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Time Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = batteryData.timeRemainingCharging!!,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (batteryData.timeRemainingState) {
                            "FINISHING" -> AppColors.StateMedium
                            "CALCULATING", "STABILIZING" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> AppColors.AccentMint
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!batteryData.isCharging && (batteryData.timeRemainingOnScreen != null || batteryData.timeRemainingOffScreen != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (batteryData.timeRemainingOnScreen != null && batteryData.timeRemainingOffScreen != null) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Onscreen",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = batteryData.timeRemainingOnScreen!!,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.StateCharging,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Offscreen",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = batteryData.timeRemainingOffScreen!!,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.StateCharging,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (batteryData.timeRemainingOnScreen != null) "Onscreen" else "Offscreen",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = batteryData.timeRemainingOnScreen ?: batteryData.timeRemainingOffScreen ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.StateCharging,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = getThermalColor(batteryData.thermalLevel).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌡️",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = batteryData.thermalStatus,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = getThermalColor(batteryData.thermalLevel),
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (batteryData.pluggedType) {
                    BatteryManager.BATTERY_PLUGGED_AC -> AppColors.StateCharging.copy(alpha = 0.15f)
                    BatteryManager.BATTERY_PLUGGED_USB -> AppColors.AccentMint.copy(alpha = 0.15f)
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> AppColors.StateMedium.copy(alpha = 0.15f)
                    else -> Color(0xff000000).copy(alpha = 0.25f)
                }
            ) {
                Text(
                    text = when (batteryData.pluggedType) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC Charging"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        else -> "On Battery"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = when (batteryData.pluggedType) {
                        BatteryManager.BATTERY_PLUGGED_AC -> AppColors.StateCharging
                        BatteryManager.BATTERY_PLUGGED_USB -> AppColors.AccentMint
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> AppColors.StateMedium
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    value: String,
    unit: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                ),
                color = color,
                maxLines = 1
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private fun getThermalColor(thermalLevel: Int): androidx.compose.ui.graphics.Color {
    return when (thermalLevel) {
        0 -> AppColors.StateCharging
        1 -> AppColors.HealthGood
        2 -> AppColors.StateMedium
        3 -> AppColors.StateLow
        4 -> AppColors.StateCritical
        else -> AppColors.TextSecondary
    }
}