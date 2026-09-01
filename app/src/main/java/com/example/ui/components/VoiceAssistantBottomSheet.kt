package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.ui.theme.SecondarySaffron
import com.example.voice.VoiceSpeechManager
import com.example.voice.VoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantBottomSheet(
    language: Language,
    voiceSpeechManager: VoiceSpeechManager,
    lastVoiceFeedback: String?,
    onDismiss: () -> Unit,
    onSimulatePhrase: (String) -> Unit
) {
    val voiceState by voiceSpeechManager.voiceState.collectAsState()
    val rmsDb by voiceSpeechManager.rmsDb.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "micAnimation")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎙️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == Language.HINDI) "बोलकर लिस्ट बनाएं" else "Voice Grocery Search",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_modal_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Visual Area with Wave Circles
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp)
            ) {
                if (voiceState is VoiceState.Listening) {
                    // Outer Ripple
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(waveScale)
                            .clip(CircleShape)
                            .background(SecondarySaffron.copy(alpha = 0.2f))
                    )
                    // Inner Ripple
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(1f + (rmsDb.coerceIn(0f, 10f) / 20f))
                            .clip(CircleShape)
                            .background(SecondarySaffron.copy(alpha = 0.35f))
                    )
                }

                // Core Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            when (voiceState) {
                                is VoiceState.Listening -> SecondarySaffron
                                is VoiceState.Processing -> PrimaryGreen
                                is VoiceState.Success -> PrimaryGreen
                                is VoiceState.Error -> MaterialTheme.colorScheme.error
                                else -> SecondarySaffron
                            }
                        )
                        .clickable {
                            if (voiceState is VoiceState.Listening) {
                                voiceSpeechManager.stopListening()
                            } else {
                                voiceSpeechManager.startListening(language)
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Text
            Text(
                text = when (voiceState) {
                    is VoiceState.Listening -> if (language == Language.HINDI) "सुन रहे हैं... बोलिए!" else "Listening... Speak now!"
                    is VoiceState.Processing -> (voiceState as VoiceState.Processing).recognizedText
                    is VoiceState.Error -> (voiceState as VoiceState.Error).errorMessage
                    else -> if (language == Language.HINDI) "माइक दबाकर बोलें" else "Tap Mic and Speak"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = when (voiceState) {
                        is VoiceState.Listening -> SecondarySaffron
                        is VoiceState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                ),
                textAlign = TextAlign.Center
            )

            // Spoken Instruction Hint
            Text(
                text = if (language == Language.HINDI)
                    "जैसे बोलें: \"2 किलो आलू और 1 पैकेट दूध\" या \"दो किलो टमाटर\""
                else
                    "Try speaking: \"2 kg potato and 1 packet milk\" or \"500g paneer\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Result / Feedback Card
            if (!lastVoiceFeedback.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryGreenContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lastVoiceFeedback,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Example Preset Chips (Accessible & Low-Literacy Friendly)
            Text(
                text = if (language == Language.HINDI) "⚡ या इन उदाहरणों पर टैप करें:" else "⚡ Or tap a quick example:",
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val samplePhrases = if (language == Language.HINDI) {
                listOf(
                    "🥔 2 किलो आलू और 1 पैकेट दूध",
                    "🍅 दो किलो टमाटर और 1 किलो प्याज",
                    "🧀 500 ग्राम पनीर और ब्रेड",
                    "🧼 2 पैकेट सर्फ एक्सेल और विम",
                    "🍚 5 किलो आटा और 2 किलो चावल",
                    "🧂 1 किलो चीनी और नमक"
                )
            } else {
                listOf(
                    "🥔 2 kg Potato and 1 Packet Milk",
                    "🍅 2 kg Tomato and 1 kg Onion",
                    "🧀 500g Paneer and 1 Bread",
                    "🧼 2 Packet Surf and Vim Bar",
                    "🍚 5 kg Atta and 2 kg Rice",
                    "🧂 1 kg Sugar and 1 Packet Salt"
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(samplePhrases) { phrase ->
                    val cleanText = phrase.substring(phrase.indexOf(" ") + 1)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onSimulatePhrase(cleanText)
                            }
                    ) {
                        Text(
                            text = phrase,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { voiceSpeechManager.startListening(language) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (language == Language.HINDI) "फिर से बोलें" else "Speak Again")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(text = if (language == Language.HINDI) "लिस्ट देखें" else "View List")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
