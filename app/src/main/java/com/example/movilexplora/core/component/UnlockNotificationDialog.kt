package com.example.movilexplora.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.movilexplora.domain.model.UnlockNotification
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@Composable
fun UnlockNotificationDialog(
    notification: UnlockNotification,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close button
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = notification.title,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DarkBlue)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Large Central Icon with Gradient and Shadow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(12.dp, CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Turquoise, Turquoise.copy(alpha = 0.7f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = notification.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = notification.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Details Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Turquoise.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Turquoise,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Fecha de obtención", fontSize = 11.sp, color = GrayText)
                                Text(text = notification.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "XP Ganada", fontSize = 11.sp, color = GrayText)
                            Text(text = notification.xpEarned, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Turquoise)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar Decorator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Turquoise)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notification.footerText,
                    fontSize = 10.sp,
                    color = GrayText.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                }
            }
        }
    }
}
