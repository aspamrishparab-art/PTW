package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PermitStatus
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBg
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralLight
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenBg

@Composable
fun PermitStatusBadge(
    status: PermitStatus,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (bgColor, textColor, icon, label) = when (status) {
        PermitStatus.APPROVED -> Quad(
            SafetyGreenBg,
            SafetyGreen,
            Icons.Default.CheckCircle,
            "Approved"
        )
        PermitStatus.PENDING -> Quad(
            AlertAmberBg,
            AlertAmber,
            Icons.Default.HourglassTop,
            "Pending"
        )
        PermitStatus.EXPIRED -> Quad(
            DangerRedBg,
            DangerRed,
            Icons.Default.Error,
            "Expired"
        )
        PermitStatus.REJECTED -> Quad(
            DangerRedBg,
            DangerRed,
            Icons.Default.Close,
            "Rejected"
        )
        PermitStatus.CLOSED -> Quad(
            NeutralLight,
            NeutralDark,
            Icons.Default.DoneAll,
            "Closed"
        )
    }

    Box(
        modifier = modifier
            .testTag("status_badge_${status.name.lowercase()}")
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(
                horizontal = if (isCompact) 8.dp else 12.dp,
                vertical = if (isCompact) 3.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label Status",
                tint = textColor,
                modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
            )
            Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = if (isCompact) 11.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
