package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun CategoryScrollTabs(
    selectedCategory: GroceryCategory?,
    onSelectCategory: (GroceryCategory?) -> Unit,
    showOnlySelected: Boolean,
    onToggleShowOnlySelected: (Boolean) -> Unit,
    allItems: List<GroceryItemEntity>,
    selectedItems: List<GroceryItemEntity>,
    language: Language
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab 1: All Items (सभी)
        val isAllActive = selectedCategory == null && !showOnlySelected
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isAllActive) PrimaryGreen else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isAllActive) PrimaryGreen else MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .height(38.dp)
                .testTag("tab_category_all")
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onToggleShowOnlySelected(false)
                    onSelectCategory(null)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(text = "✨", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (language == Language.HINDI) "सभी सामान" else "All Items",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isAllActive) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isAllActive) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${allItems.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tab 2: Only Selected (चुने गए)
        val isSelectedTabActive = showOnlySelected
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelectedTabActive) Color(0xFFB91C1C) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelectedTabActive) Color(0xFFB91C1C) else MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .height(38.dp)
                .testTag("tab_category_selected")
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onToggleShowOnlySelected(!showOnlySelected)
                    if (!showOnlySelected) {
                        onSelectCategory(null)
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(text = "✅", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (language == Language.HINDI) "चुने गए" else "Selected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelectedTabActive) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelectedTabActive) Color.White else Color(0xFFB91C1C).copy(alpha = 0.12f))
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${selectedItems.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelectedTabActive) Color(0xFFB91C1C) else Color(0xFFB91C1C)
                    )
                }
            }
        }

        // Preloaded Categories Tabs (Color Coded & Accessible)
        for (category in GroceryCategory.displayCategories) {
            val isCatActive = selectedCategory == category && !showOnlySelected
            val countInCat = allItems.count { it.category == category.id }
            val selectedInCat = selectedItems.count { it.category == category.id }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCatActive) category.primaryColor else category.backgroundColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCatActive) category.primaryColor else category.primaryColor.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("tab_category_${category.id.lowercase()}")
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onToggleShowOnlySelected(false)
                        onSelectCategory(category)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(text = category.emoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = category.getDisplayName(language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isCatActive) Color.White else category.primaryColor
                    )

                    if (selectedInCat > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCatActive) Color.White else category.primaryColor)
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$selectedInCat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCatActive) category.primaryColor else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
