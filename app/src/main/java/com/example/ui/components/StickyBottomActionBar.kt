package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.ui.theme.SecondarySaffron
import com.example.ui.theme.WhatsAppBrandGreen

@Composable
fun StickyBottomActionBar(
    selectedCount: Int,
    language: Language,
    onOpenVoiceAssistant: () -> Unit,
    onOpenReview: () -> Unit,
    onDirectSharePdf: () -> Unit
) {
    // Pulse animation for central microphone button
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left: Selected Items Badge / Review Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedCount > 0) PrimaryGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedCount > 0) PrimaryGreen else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("selected_summary_button")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenReview() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgedBox(
                            badge = {
                                if (selectedCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFB91C1C),
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "$selectedCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = if (selectedCount > 0) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "$selectedCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedCount > 0) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == Language.HINDI) "सामान" else "Items",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Center: Large Glowing Floating Microphone Button (Speech to Text)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .scale(pulseScale)
                        .testTag("voice_search_fab")
                        .clip(RoundedCornerShape(10.dp))
                        .background(SecondarySaffron)
                        .clickable { onOpenVoiceAssistant() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Right: Big Prominent "दुकानदार को भेजें / Send List" Button
                Button(
                    onClick = onDirectSharePdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("send_list_to_shopkeeper_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCount > 0) WhatsAppBrandGreen else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selectedCount > 0) 1.dp else 0.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📄", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = if (language == Language.HINDI) "दुकानदार को भेजें" else "Send to Shopkeeper",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (language == Language.HINDI) "PDF व WhatsApp लिस्ट" else "PDF & WhatsApp Slip",
                                fontSize = 10.sp,
                                color = if (selectedCount > 0) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
