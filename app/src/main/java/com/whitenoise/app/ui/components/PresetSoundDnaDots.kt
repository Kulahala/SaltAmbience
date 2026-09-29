package com.whitenoise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

/**
 * Bauhaus Sound DNA Dots for Preset Cards.
 * Renders a compact row of up to 4 micro-dots representing the acoustic ingredients of the preset.
 * Clamps visible dots to 3 + "+N" badge if exceeding 4 tracks to guarantee strict layout boundaries
 * and prevent stretching the preset card horizontally.
 */
@Composable
fun PresetSoundDnaDots(
    trackIds: List<String>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    if (trackIds.isEmpty()) return

    val maxVisible = 4
    val visibleTracks = if (trackIds.size <= maxVisible) trackIds else trackIds.take(3)
    val overflowCount = trackIds.size - visibleTracks.size

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.5.dp)
    ) {
        visibleTracks.forEach { trackId ->
            val color = getTrackIndividualColor(trackId, isDark)
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        if (overflowCount > 0) {
            Text(
                text = "+$overflowCount",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SaltTheme.colors.text.copy(alpha = 0.50f),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
