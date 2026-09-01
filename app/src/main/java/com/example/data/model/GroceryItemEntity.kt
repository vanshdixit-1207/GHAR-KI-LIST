package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nameHindi: String,
    val nameEnglish: String,
    val category: String, // from GroceryCategory.id
    val iconEmoji: String,
    val isSelected: Boolean = false,
    val quantity: String = "1 kg",
    val availableUnits: String = "500g,1 kg,2 kg,1 Packet,2 Packet,1 Piece",
    val customNote: String = "",
    val keywords: String = "", // Comma-separated search terms for Hindi/English/Hinglish
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
) {
    fun getName(language: Language): String {
        return if (language == Language.HINDI) nameHindi else nameEnglish
    }

    fun getSecondaryName(language: Language): String {
        return if (language == Language.HINDI) nameEnglish else nameHindi
    }

    fun getUnitList(): List<String> {
        return if (availableUnits.isNotBlank()) {
            availableUnits.split(",").map { it.trim() }
        } else {
            DEFAULT_UNITS
        }
    }

    companion object {
        val DEFAULT_UNITS = listOf("500g", "1 kg", "2 kg", "5 kg", "1 Packet", "2 Packet", "1 Piece", "1 Dozen", "1 L", "2 L")
        val COMMON_UNITS = listOf("500g", "1 kg", "2 kg", "1 Packet", "2 Packet", "1 Piece")
    }
}
