package com.example.battery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.ui.theme.*
import com.example.battery.ui.viewmodel.MainViewModel

@Composable
fun ConfigScreen(
    viewModel: MainViewModel,
    onConfigComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configState by viewModel.configUiState.collectAsState()
    var capacityInput by remember { mutableStateOf("") }
    var hasInitialized by remember { mutableStateOf(false) }

    // Initialize config data ONCE on first composition
    LaunchedEffect(Unit) {
        if (!hasInitialized) {
            viewModel.initializeConfigData()
            hasInitialized = true
        }
    }

    // Update capacity input when config state provides detected capacity
    LaunchedEffect(configState.batteryCapacity) {
        if (capacityInput.isEmpty() && configState.batteryCapacity.isNotEmpty()) {
            capacityInput = configState.batteryCapacity
        }
    }

    // Start collecting current samples ONCE after device info is loaded
    LaunchedEffect(configState.deviceInfo, configState.collectedValues.isEmpty()) {
        if (configState.deviceInfo.isNotEmpty() &&
            configState.collectedValues.isEmpty() &&
            !configState.isCollecting) {
            viewModel.collectCurrentSamples()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Battery Configuration",
                style = MaterialTheme.typography.headlineMedium,
                color = ElectricBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Device Info Card
            InfoCard(
                title = "Device Information",
                content = configState.deviceInfo.ifEmpty { "Loading device information..." }
            )

            // Current Unit Card
            InfoCard(
                title = "Current Detection",
                content = configState.currentUnit.ifEmpty { "Detecting current unit..." }
            )

            // Current Pattern Card
            InfoCard(
                title = "Current Pattern",
                content = configState.currentPattern,
                isLoading = configState.isCollecting
            )

            // Battery Capacity Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Battery Capacity",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = capacityInput,
                        onValueChange = {
                            capacityInput = it
                            viewModel.updateBatteryCapacity(it)
                        },
                        label = { Text("Capacity (mAh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextTertiary,
                            focusedLabelColor = ElectricBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = ElectricBlue,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceLight,
                            unfocusedContainerColor = SurfaceLight
                        ),
                        textStyle = TeckyTextStyles.NumericMedium
                    )

                    if (capacityInput.isEmpty() || capacityInput.toIntOrNull() == null) {
                        Text(
                            text = "⚠ Please enter a valid battery capacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonRed,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val instructionText = when {
                        configState.deviceInfo.isEmpty() ->
                            "⏳ Loading device information..."
                        configState.isCollecting ->
                            "⚡ Collecting battery data... Please wait.\n\n" +
                                    "Ensure your device is charging for accurate readings."
                        configState.collectedValues.isEmpty() ->
                            "⏳ Waiting to start data collection..."
                        else ->
                            "✅ Data collection complete!\n\n" +
                                    "Review the information above and tap Configure to continue."
                    }

                    Text(
                        text = instructionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Configure Button
            Button(
                onClick = {
                    val capacity = capacityInput.toIntOrNull()
                    if (capacity != null && capacity > 0) {
                        viewModel.saveConfiguration(capacity)
                        onConfigComplete()
                    }
                },
                enabled = configState.canConfigure &&
                        capacityInput.toIntOrNull() != null &&
                        capacityInput.toInt() > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = TextTertiary
                )
            ) {
                Text(
                    text = if (configState.isCollecting) "Collecting..." else "Configure",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BackgroundBlack
                )
            }

            // Debug info (shows sample count)
            if (configState.collectedValues.isNotEmpty()) {
                Text(
                    text = "Collected ${configState.collectedValues.size} samples",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: String,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElectricBlue,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = content,
                        style = TeckyTextStyles.NumericSmall,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = content,
                    style = TeckyTextStyles.NumericSmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}