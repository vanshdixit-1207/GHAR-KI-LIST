package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
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
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import com.example.data.model.Language
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryItemCard(
    item: GroceryItemEntity,
    language: Language,
    onToggleSelect: () -> Unit,
    onUpdateQuantity: (String) -> Unit,
    onDeleteItem: (() -> Unit)? = null
) {
    val category = GroceryCategory.fromId(item.category)
    var showQuantityDropdown by remember { mutableStateOf(false) }

    val cardBorderColor by animateColorAsState(
        targetValue = if (item.isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        label = "cardBorderColor"
    )

    val cardElevation by animateDpAsState(
        targetValue = if (item.isSelected) 2.dp else 0.dp,
        label = "cardElevation"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (item.isSelected) PrimaryGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(if (item.isSelected) 1.5.dp else 1.dp, cardBorderColor),
        shadowElevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggleSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Large Touch-Friendly Checkbox (Accessible >= 48dp target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("checkbox_${item.id}")
                    .clip(CircleShape)
                    .clickable { onToggleSelect() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.isSelected) PrimaryGreen else Color.Transparent
                        )
                        .border(
                            2.dp,
                            if (item.isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 2. High-Density Visual Emoji / Icon Representation (38dp rounded rectangle)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(category.backgroundColor)
                    .border(1.dp, category.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.iconEmoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 3. Item Name in Hindi & English (High Contrast Crisp Slate Typography)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.getName(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (item.isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )

                Text(
                    text = item.getSecondaryName(language),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 4. Inline Quantity & Packet Selector Chip / Dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isSelected) category.primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isSelected) category.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .testTag("quantity_selector_${item.id}")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showQuantityDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.quantity,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (item.isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Quantity",
                            tint = if (item.isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Dropdown Menu with Indian grocery units
                DropdownMenu(
                    expanded = showQuantityDropdown,
                    onDismissRequest = { showQuantityDropdown = false },
                    modifier = Modifier.widthIn(min = 160.dp)
                ) {
                    Text(
                        text = if (language == Language.HINDI) "मात्रा चुनें / Select Qty" else "Select Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider()

                    val units = item.getUnitList()
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = unit,
                                    fontWeight = if (item.quantity == unit) FontWeight.Bold else FontWeight.Normal,
                                    color = if (item.quantity == unit) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = {
                                if (item.quantity == unit) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Current Quantity",
                                        tint = PrimaryGreen
                                    )
                                }
                            },
                            onClick = {
                                onUpdateQuantity(unit)
                                if (!item.isSelected) {
                                    onToggleSelect()
                                }
                                showQuantityDropdown = false
                            }
                        )
                    }
                }
            }

            // Optional delete button for custom-added items
            if (item.isCustom && onDeleteItem != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDeleteItem,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_custom_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
