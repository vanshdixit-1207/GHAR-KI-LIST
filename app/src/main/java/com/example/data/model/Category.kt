package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GroceryCategory(
    val id: String,
    val nameHindi: String,
    val nameEnglish: String,
    val emoji: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val order: Int
) {
    VEGETABLES_FRUITS(
        id = "VEGETABLES_FRUITS",
        nameHindi = "सब्ज़ी और फल",
        nameEnglish = "Sabzi & Phal",
        emoji = "🥦",
        primaryColor = CatVegGreen,
        backgroundColor = CatVegGreenBg,
        order = 1
    ),
    GRAINS_PULSES(
        id = "GRAINS_PULSES",
        nameHindi = "दाल और अनाज",
        nameEnglish = "Daal & Anaj",
        emoji = "🌾",
        primaryColor = CatGrainsAmber,
        backgroundColor = CatGrainsAmberBg,
        order = 2
    ),
    SPICES_PANTRY(
        id = "SPICES_PANTRY",
        nameHindi = "मसाले और किराना",
        nameEnglish = "Masale & Grocery",
        emoji = "🌶️",
        primaryColor = CatSpicesRed,
        backgroundColor = CatSpicesRedBg,
        order = 3
    ),
    DAIRY(
        id = "DAIRY",
        nameHindi = "दूध और डेयरी",
        nameEnglish = "Doodh & Dairy",
        emoji = "🥛",
        primaryColor = CatDairyBlue,
        backgroundColor = CatDairyBlueBg,
        order = 4
    ),
    CLEANING_HOUSEHOLD(
        id = "CLEANING_HOUSEHOLD",
        nameHindi = "घर की सफाई",
        nameEnglish = "Ghar ki Safai",
        emoji = "🧹",
        primaryColor = CatCleaningPurple,
        backgroundColor = CatCleaningPurpleBg,
        order = 5
    ),
    OTHER(
        id = "OTHER",
        nameHindi = "अन्य सामान",
        nameEnglish = "Other Items",
        emoji = "📦",
        primaryColor = CatOtherSlate,
        backgroundColor = CatOtherSlateBg,
        order = 6
    );

    fun getDisplayName(language: Language): String {
        return if (language == Language.HINDI) nameHindi else nameEnglish
    }

    companion object {
        fun fromId(id: String): GroceryCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }

        val displayCategories: List<GroceryCategory> = listOf(
            VEGETABLES_FRUITS,
            GRAINS_PULSES,
            SPICES_PANTRY,
            DAIRY,
            CLEANING_HOUSEHOLD,
            OTHER
        )
    }
}
