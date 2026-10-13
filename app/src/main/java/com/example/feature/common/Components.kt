package com.example.feature.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary
import com.example.ui.theme.NinjaShadowGold

@Composable
fun ShurikenIcon(modifier: Modifier = Modifier, size: Int = 24) {
    val infiniteTransition = rememberInfiniteTransition(label = "shuriken_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        // Core cross
        Box(
            modifier = Modifier
                .size((size * 0.9).dp, (size * 0.28).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(NinjaPrimary, NinjaAccentCyan)))
        )
        Box(
            modifier = Modifier
                .size((size * 0.28).dp, (size * 0.9).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.verticalGradient(listOf(NinjaPrimary, NinjaAccentCyan)))
        )
        // Center ring
        Box(
            modifier = Modifier
                .size((size * 0.35).dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun DeliveryStatusTicks(
    status: String,
    modifier: Modifier = Modifier
) {
    when (status) {
        "QUEUED" -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Queued",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(14.dp)
            )
        }
        "SENDING" -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Sending",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(14.dp)
            )
        }
        "SENT" -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(15.dp)
            )
        }
        "DELIVERED" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(16.dp)
            )
        }
        "READ" -> {
            // WhatsApp violet/cyan double check ticks
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = NinjaAccentCyan,
                modifier = modifier.size(16.dp)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun ShadowModeBadge(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) NinjaShadowGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, NinjaShadowGold) else null,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggle() }
            .testTag("shadow_mode_toggle_btn")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Shadow Mode",
                tint = if (isActive) NinjaShadowGold else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isActive) "SHADOW ON" else "SHADOW",
                color = if (isActive) NinjaShadowGold else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    durationSec: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 20 simulated waveform bars
        val heights = listOf(
            0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 1.0f, 0.6f, 0.3f,
            0.7f, 0.9f, 0.8f, 0.4f, 0.6f, 0.85f, 0.5f, 0.9f,
            0.7f, 0.4f, 0.6f, 0.3f
        )
        heights.forEachIndexed { index, h ->
            val barColor = if (isPlaying && index < 10) NinjaAccentCyan else NinjaPrimary.copy(alpha = 0.7f)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
            Spacer(modifier = Modifier.width(2.5.dp))
        }
    }
}

@Composable
fun SecurityShieldBadge(
    isSealedSender: Boolean = true,
    isVerified: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NinjaPrimary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "E2EE Verified",
            tint = if (isVerified) NinjaEmeraldTick else NinjaAccentCyan,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isVerified) "E2EE VERIFIED" else "SEALED E2EE",
            color = if (isVerified) NinjaEmeraldTick else NinjaAccentCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
