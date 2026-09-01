package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GroceryCategory
import com.example.data.model.Language
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomItemDialog(
    language: Language,
    onDismiss: () -> Unit,
    onAddItem: (nameHindi: String, nameEnglish: String, category: GroceryCategory, iconEmoji: String, quantity: String) -> Unit
) {
    var nameHindi by remember { mutableStateOf("") }
    var nameEnglish by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GroceryCategory.VEGETABLES_FRUITS) }
    var selectedEmoji by remember { mutableStateOf("🛍️") }
    var selectedQuantity by remember { mutableStateOf("1 kg") }

    val emojis = listOf(
        "🥔", "🍅", "🧅", "🧄", "🌶️", "🥦", "🥕", "🥬", "🍎", "🍌",
        "🌾", "🍚", "🥣", "🫘", "🧂", "🍬", "🍵", "🫗", "🧈",
        "🥛", "🧀", "🍞", "🥚", "🧼", "🧽", "🧴", "🚽", "🧹",
        "🍪", "🍫", "🧃", "💊", "📦", "🛍️"
    )

    val quantityOptions = listOf("500g", "1 kg", "2 kg", "5 kg", "1 Packet", "2 Packet", "1 Piece", "2 Piece", "1 L", "2 L", "1 Dozen")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "➕", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.HINDI) "नया सामान जोड़ें" else "Add New Item",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Emoji Avatar Picker
                Text(
                    text = if (language == Language.HINDI) "1. चित्र/आइकन चुनें:" else "1. Choose Visual Icon:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { emoji ->
                        val isSelected = emoji == selectedEmoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryGreenContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    if (isSelected) PrimaryGreen else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Name Fields
                OutlinedTextField(
                    value = nameHindi,
                    onValueChange = { nameHindi = it },
                    label = { Text(if (language == Language.HINDI) "सामान का नाम (हिन्दी)" else "Item Name (Hindi)") },
                    placeholder = { Text("उदा. पनीर, बिस्किट, साबुन") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_item_hindi_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameEnglish,
                    onValueChange = { nameEnglish = it },
                    label = { Text(if (language == Language.HINDI) "अंग्रेजी नाम (वैकल्पिक)" else "Item Name (English)") },
                    placeholder = { Text("e.g. Biscuit, Paneer, Soap") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_item_english_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Category Selector
                Text(
                    text = if (language == Language.HINDI) "2. श्रेणी चुनें:" else "2. Select Category:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val catScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(catScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroceryCategory.displayCategories.forEach { category ->
                        val isCatSelected = selectedCategory == category
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCatSelected) category.primaryColor else category.backgroundColor,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isCatSelected) category.primaryColor else category.primaryColor.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = category }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category.getDisplayName(language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCatSelected) Color.White else category.primaryColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Default Quantity
                Text(
                    text = if (language == Language.HINDI) "3. मात्रा (Quantity):" else "3. Quantity:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val qtyScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(qtyScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quantityOptions.forEach { qty ->
                        val isQtySelected = selectedQuantity == qty
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isQtySelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedQuantity = qty }
                        ) {
                            Text(
                                text = qty,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isQtySelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalHindi = nameHindi.trim().ifBlank { nameEnglish.trim() }
                        val finalEnglish = nameEnglish.trim().ifBlank { nameHindi.trim() }
                        if (finalHindi.isNotBlank() || finalEnglish.isNotBlank()) {
                            onAddItem(
                                finalHindi,
                                finalEnglish,
                                selectedCategory,
                                selectedEmoji,
                                selectedQuantity
                            )
                        }
                    },
                    enabled = nameHindi.isNotBlank() || nameEnglish.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_custom_item_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == Language.HINDI) "लिस्ट में जोड़ें" else "Add to List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
