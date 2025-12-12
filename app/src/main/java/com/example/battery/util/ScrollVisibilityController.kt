package com.example.battery.util

import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ScrollVisibilityController
 *
 * - Scrolling up = hide navbar
 * - Scrolling down = show navbar
 */
@Stable
class ScrollVisibilityController {

    private val _isVisible = MutableStateFlow(true)
    val isVisible: StateFlow<Boolean> = _isVisible

    val nestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            if (dy < 0) {
                // scrolling upward -> hide navbar
                _isVisible.value = false
            } else if (dy > 0) {
                // scrolling downward -> show navbar
                _isVisible.value = true
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity {
            // Do not change visibility on fling end; just return consumed velocity
            return Velocity.Zero
        }
    }

    fun show() { _isVisible.value = true }
    fun hide() { _isVisible.value = false }
    fun reset() { _isVisible.value = true }
}

@Composable
fun rememberScrollVisibilityController(): ScrollVisibilityController {
    return remember { ScrollVisibilityController() }
}
