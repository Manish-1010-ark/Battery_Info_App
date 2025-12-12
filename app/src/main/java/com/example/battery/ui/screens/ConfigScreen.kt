package com.example.battery.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.ui.viewmodel.MainViewModel

// Material Symbols Icons
object MaterialSymbols {
    val BatteryChargingFull: ImageVector
        get() = ImageVector.Builder(
            name = "BatteryChargingFull",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF000000))) {
                moveTo(660f, 880f)
                verticalLineToRelative(-120f)
                horizontalLineTo(560f)
                lineToRelative(140f, -200f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(100f)
                lineTo(660f, 880f)
                close()
                moveTo(320f, 960f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(280f, 920f)
                verticalLineTo(280f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(320f, 240f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(80f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(680f, 280f)
                verticalLineToRelative(280f)
                quadToRelative(-21f, 0f, -41f, 3.5f)
                reflectiveQuadTo(600f, 574f)
                verticalLineTo(320f)
                horizontalLineTo(360f)
                verticalLineToRelative(560f)
                horizontalLineToRelative(94f)
                quadToRelative(8f, 23f, 19.5f, 43f)
                reflectiveQuadTo(501f, 960f)
                horizontalLineTo(320f)
                close()
            }
        }.build()

    val Smartphone: ImageVector
        get() = ImageVector.Builder(
            name = "Smartphone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF000000))) {
                moveTo(280f, 1000f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(200f, 920f)
                verticalLineTo(200f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(280f, 120f)
                horizontalLineToRelative(400f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(760f, 200f)
                verticalLineToRelative(720f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(680f, 1000f)
                horizontalLineTo(280f)
                close()
                moveTo(280f, 880f)
                verticalLineToRelative(40f)
                horizontalLineToRelative(400f)
                verticalLineToRelative(-40f)
                horizontalLineTo(280f)
                close()
                moveTo(280f, 800f)
                horizontalLineToRelative(400f)
                verticalLineTo(320f)
                horizontalLineTo(280f)
                verticalLineToRelative(480f)
                close()
                moveTo(280f, 240f)
                horizontalLineToRelative(400f)
                verticalLineToRelative(-40f)
                horizontalLineTo(280f)
                verticalLineToRelative(40f)
                close()
            }
        }.build()

    val BoltFilled: ImageVector
        get() = ImageVector.Builder(
            name = "Bolt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF000000))) {
                moveTo(280f, 880f)
                lineToRelative(160f, -300f)
                horizontalLineTo(320f)
                lineToRelative(120f, -440f)
                horizontalLineToRelative(240f)
                lineTo(512f, 440f)
                horizontalLineToRelative(168f)
                lineTo(280f, 880f)
                close()
            }
        }.build()

    val Speed: ImageVector
        get() = ImageVector.Builder(
            name = "Speed",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF000000))) {
                moveTo(418f, 620f)
                quadToRelative(24f, 24f, 62f, 23.5f)
                reflectiveQuadToRelative(56f, -27.5f)
                lineToRelative(164f, -260f)
                lineToRelative(-260f, 164f)
                quadToRelative(-27f, 18f, -28f, 55f)
                reflectiveQuadToRelative(22f, 63f)
                close()
                moveTo(480f, 160f)
                quadToRelative(59f, 0f, 113.5f, 16.5f)
                reflectiveQuadTo(696f, 226f)
                lineToRelative(-76f, 48f)
                quadToRelative(-33f, -17f, -68.5f, -25.5f)
                reflectiveQuadTo(480f, 240f)
                quadToRelative(-133f, 0f, -226.5f, 93.5f)
                reflectiveQuadTo(160f, 560f)
                quadToRelative(0f, 42f, 11.5f, 83f)
                reflectiveQuadToRelative(32.5f, 77f)
                horizontalLineToRelative(552f)
                quadToRelative(23f, -38f, 33.5f, -79f)
                reflectiveQuadToRelative(10.5f, -85f)
                quadToRelative(0f, -36f, -8.5f, -70f)
                reflectiveQuadTo(766f, 420f)
                lineToRelative(48f, -76f)
                quadToRelative(30f, 47f, 47.5f, 100f)
                reflectiveQuadTo(880f, 554f)
                quadToRelative(1f, 57f, -13f, 109f)
                reflectiveQuadToRelative(-41f, 99f)
                quadToRelative(-11f, 18f, -30f, 28f)
                reflectiveQuadToRelative(-40f, 10f)
                horizontalLineTo(204f)
                quadToRelative(-21f, 0f, -40f, -10f)
                reflectiveQuadToRelative(-30f, -28f)
                quadToRelative(-26f, -45f, -40f, -95.5f)
                reflectiveQuadTo(80f, 560f)
                quadToRelative(0f, -83f, 31.5f, -155.5f)
                reflectiveQuadToRelative(86f, -127f)
                quadTo(252f, 223f, 325f, 191.5f)
                reflectiveQuadTo(480f, 160f)
                close()
            }
        }.build()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ConfigScreen(
    viewModel: MainViewModel,
    onConfigComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // CRITICAL: Read BOTH config state AND live battery data
    val configState by viewModel.configUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // CRITICAL: Use real-time battery data for charging status
    val isCharging = uiState.batteryData.isCharging

    Log.d("ConfigScreen", "🔋 STATE: isCharging=$isCharging, isCollecting=${configState.isCollecting}, samplesCount=${configState.collectedValues.size}")

    var capacityInput by remember { mutableStateOf("") }
    var hasInitialized by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // CRITICAL: Track if sampling has ever been attempted
    var samplingAttempted by remember { mutableStateOf(false) }

    // Initialize config data ONCE on first composition
    LaunchedEffect(Unit) {
        if (!hasInitialized) {
            viewModel.initializeConfigData()
            hasInitialized = true
            kotlinx.coroutines.delay(100)
            showContent = true
        }
    }

    // Update capacity input when config state provides detected capacity
    LaunchedEffect(configState.batteryCapacity) {
        if (capacityInput.isEmpty() && configState.batteryCapacity.isNotEmpty()) {
            capacityInput = configState.batteryCapacity
        }
    }

    // CRITICAL: Single LaunchedEffect for sampling control
    LaunchedEffect(
        configState.deviceInfo.isNotEmpty(),
        configState.collectedValues.isEmpty(),
        isCharging,
        configState.isCollecting,
        samplingAttempted
    ) {
        Log.d("ConfigScreen", "🔄 LaunchedEffect TRIGGERED - deviceInfo=${configState.deviceInfo.isNotEmpty()}, valuesEmpty=${configState.collectedValues.isEmpty()}, isCharging=$isCharging, isCollecting=${configState.isCollecting}, samplingAttempted=$samplingAttempted")

        val deviceInfoReady = configState.deviceInfo.isNotEmpty()
        val valuesNotCollected = configState.collectedValues.isEmpty()
        val notCurrentlyCollecting = !configState.isCollecting
        val notCharging = !isCharging

        Log.d("ConfigScreen", "📊 CONDITIONS: deviceInfoReady=$deviceInfoReady, valuesNotCollected=$valuesNotCollected, notCurrentlyCollecting=$notCurrentlyCollecting, notCharging=$notCharging, samplingAttempted=$samplingAttempted")

        if (deviceInfoReady &&
            valuesNotCollected &&
            notCurrentlyCollecting &&
            notCharging &&
            !samplingAttempted) {

            Log.d("ConfigScreen", "✅ ALL CONDITIONS MET - Starting sampling")
            samplingAttempted = true
            viewModel.collectCurrentSamples()
        } else {
            Log.d("ConfigScreen", "❌ CONDITIONS NOT MET - Not starting")
        }

        if (isCharging && configState.isCollecting) {
            Log.d("ConfigScreen", "🛑 HARD STOP: Charging detected during collection")
            viewModel.stopCurrentSampling()
            samplingAttempted = false
        }
    }

    // Form validation - CRITICAL: Only allow configuration when NOT charging
    val isFormValid = remember(capacityInput, configState.canConfigure, isCharging) {
        capacityInput.toIntOrNull()?.let { it > 0 } == true &&
                configState.canConfigure &&
                !isCharging
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OnboardingMascot()

                // Header Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome! Let's Configure",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your Battery Monitor",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "We'll detect your device's battery specifications and calibrate monitoring for maximum accuracy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }

                // Device Information Section
                SectionHeader(
                    title = "Device Information",
                    description = "Automatically detected from your device"
                )

                // CRITICAL: Charging Warning Card (Most Prominent)
                AnimatedVisibility(
                    visible = isCharging,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    ChargingWarningCard()
                }

                AnimatedVisibility(
                    visible = configState.deviceInfo.isNotEmpty(),
                    enter = fadeIn() + expandVertically()
                ) {
                    ModernInfoCard(
                        icon = MaterialSymbols.Smartphone,
                        title = "Device Details",
                        content = configState.deviceInfo,
                        isLoading = false
                    )
                }

                // Battery Specifications Section
                SectionHeader(
                    title = "Battery Specifications",
                    description = "Essential for accurate monitoring"
                )

                ModernTextField(
                    value = capacityInput,
                    onValueChange = {
                        capacityInput = it
                        viewModel.updateBatteryCapacity(it)
                    },
                    label = "Battery Capacity",
                    placeholder = "e.g., 5000",
                    helperText = "Enter your battery capacity in mAh (usually 3000-6000)",
                    icon = MaterialSymbols.BatteryChargingFull,
                    isError = capacityInput.isNotEmpty() && capacityInput.toIntOrNull() == null,
                    errorText = "Please enter a valid number",
                    enabled = !isCharging
                )

                // Current Detection Section
                SectionHeader(
                    title = "Charging Behavior",
                    description = "Analyzing your device's power characteristics"
                )

                AnimatedVisibility(
                    visible = configState.currentUnit.isNotEmpty(),
                    enter = fadeIn() + expandVertically()
                ) {
                    ModernInfoCard(
                        icon = MaterialSymbols.BoltFilled,
                        title = "Current Unit",
                        content = configState.currentUnit.replace("Current Unit: ", ""),
                        isLoading = false
                    )
                }

                AnimatedVisibility(
                    visible = configState.currentPattern.isNotEmpty() && !isCharging,
                    enter = fadeIn() + expandVertically()
                ) {
                    ModernInfoCard(
                        icon = MaterialSymbols.Speed,
                        title = "Current Pattern",
                        content = if (configState.isCollecting) {
                            "Collecting samples... ${configState.collectedValues.size}/5"
                        } else {
                            configState.currentPattern.replace("Current Pattern: ", "")
                        },
                        isLoading = configState.isCollecting
                    )
                }

                // Show retry button if sampling was interrupted
                if (!isCharging &&
                    configState.collectedValues.isEmpty() &&
                    !configState.isCollecting &&
                    samplingAttempted &&
                    configState.deviceInfo.isNotEmpty()) {

                    OutlinedButton(
                        onClick = {
                            samplingAttempted = false
                            viewModel.resetSamplingState()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry Sample Collection")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Continue Button
                ContinueButton(
                    enabled = isFormValid && !isSubmitting,
                    isLoading = isSubmitting || configState.isCollecting,
                    onClick = {
                        val capacity = capacityInput.toIntOrNull()
                        if (capacity != null && capacity > 0 && !isCharging) {
                            isSubmitting = true
                            viewModel.saveConfiguration(capacity)
                            onConfigComplete()
                        }
                    }
                )

                // Success indicator
                AnimatedVisibility(
                    visible = configState.collectedValues.isNotEmpty() &&
                            !configState.isCollecting &&
                            !isCharging,
                    enter = fadeIn() + expandVertically()
                ) {
                    Text(
                        text = "✓ Collected ${configState.collectedValues.size} samples successfully",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingMascot() {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Icon(
            imageVector = MaterialSymbols.BatteryChargingFull,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    helperText: String,
    icon: ImageVector,
    isError: Boolean = false,
    errorText: String = "",
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        isError -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            },
            enabled = enabled,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = if (isError) 0.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = if (isError) errorText else helperText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun ModernInfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(50.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Setting up...",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ChargingWarningCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            Color(0xFFFF6B35) // Vibrant orange accent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large warning icon
                Icon(
                    imageVector = ImageVector.Builder(
                        name = "Warning",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f
                    ).apply {
                        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
                            moveTo(1f, 21f)
                            horizontalLineToRelative(22f)
                            lineTo(12f, 2f)
                            lineTo(1f, 21f)
                            close()
                            moveTo(13f, 18f)
                            horizontalLineToRelative(-2f)
                            verticalLineToRelative(-2f)
                            horizontalLineToRelative(2f)
                            verticalLineToRelative(2f)
                            close()
                            moveTo(13f, 14f)
                            horizontalLineToRelative(-2f)
                            verticalLineToRelative(-4f)
                            horizontalLineToRelative(2f)
                            verticalLineToRelative(4f)
                            close()
                        }
                    }.build(),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )

                // Title
                Text(
                    text = "Charger Detected",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "Please disconnect your charger",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Body text
                Text(
                    text = "We need accurate discharge readings for proper calibration",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}