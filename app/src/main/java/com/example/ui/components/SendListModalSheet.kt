package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import com.example.data.model.Language
import com.example.pdf.PdfShareManager
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.ui.theme.WhatsAppBrandGreen
import com.example.ui.theme.WhatsAppDarkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendListModalSheet(
    selectedItems: List<GroceryItemEntity>,
    language: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var storeName by remember { mutableStateOf("") }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📄", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "दुकानदार को लिस्ट भेजें" else "Send List to Shopkeeper",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${selectedItems.size} ${if (language == Language.HINDI) "सामान चुने गए हैं" else "items selected"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_send_sheet_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Store Name / Note Field
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = {
                    Text(
                        text = if (language == Language.HINDI) "दुकान का नाम या नोट (वैकल्पिक)" else "Kirana Store Name or Note (Optional)",
                        fontSize = 13.sp
                    )
                },
                placeholder = {
                    Text(
                        text = if (language == Language.HINDI) "उदा. गुप्ता जी किराना स्टोर" else "e.g. Gupta Kirana Store",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Store, contentDescription = "Store", tint = PrimaryGreen)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Items Preview List
            Text(
                text = if (language == Language.HINDI) "📋 चुनी गई पर्ची की झलक:" else "📋 Selected Items Preview:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == Language.HINDI) "कोई सामान नहीं चुना गया है। कृपया पहले सामान चुनें।" else "No items selected. Please select items from list.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val grouped = selectedItems.groupBy { it.category }
                        for ((catKey, itemsInCat) in grouped) {
                            val category = GroceryCategory.fromId(catKey)
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = category.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.getDisplayName(language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = category.primaryColor
                                    )
                                }
                            }

                            items(itemsInCat) { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = item.iconEmoji, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.getName(language),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = item.getSecondaryName(language),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Quantity Chip
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryGreenContainer,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = item.quantity,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = PrimaryGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Action 1: PDF Document Generation & WhatsApp Sharing
                Button(
                    onClick = {
                        if (selectedItems.isNotEmpty()) {
                            isGeneratingPdf = true
                            val pdfFile = PdfShareManager.generateGroceryPdf(
                                context = context,
                                selectedItems = selectedItems,
                                language = language,
                                storeOrFamilyName = storeName
                            )
                            isGeneratingPdf = false
                            if (pdfFile != null) {
                                PdfShareManager.sharePdfFile(context, pdfFile, language)
                            }
                        }
                    },
                    enabled = selectedItems.isNotEmpty() && !isGeneratingPdf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_share_pdf_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = if (language == Language.HINDI) "PDF बन रहा है..." else "Generating PDF...")
                    } else {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.HINDI) "PDF पर्ची बनाएं और WhatsApp पर भेजें" else "Generate & Share PDF Grocery Slip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Secondary Action 2: Direct WhatsApp Text Share & Copy Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // WhatsApp Text Message Button
                    Button(
                        onClick = {
                            if (selectedItems.isNotEmpty()) {
                                val message = PdfShareManager.formatWhatsAppMessage(
                                    items = selectedItems,
                                    language = language,
                                    storeOrFamilyName = storeName
                                )
                                PdfShareManager.shareTextToWhatsApp(context, message, language)
                            }
                        },
                        enabled = selectedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_whatsapp_text_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppBrandGreen)
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == Language.HINDI) "WhatsApp मैसेज" else "WhatsApp Text",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    // Copy to Clipboard Button
                    OutlinedButton(
                        onClick = {
                            if (selectedItems.isNotEmpty()) {
                                val message = PdfShareManager.formatWhatsAppMessage(
                                    items = selectedItems,
                                    language = language,
                                    storeOrFamilyName = storeName
                                )
                                PdfShareManager.copyToClipboard(context, message)
                            }
                        },
                        enabled = selectedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("copy_list_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == Language.HINDI) "कॉपी करें" else "Copy Text",
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
