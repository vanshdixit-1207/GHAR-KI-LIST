package com.example.voice

import com.example.data.model.GroceryItemEntity
import java.util.Locale

data class ParsedVoiceItem(
    val matchedItem: GroceryItemEntity,
    val parsedQuantity: String,
    val rawMatchedPhrase: String
)

data class VoiceParseResult(
    val rawText: String,
    val matchedItems: List<ParsedVoiceItem>,
    val unrecognizedTokens: List<String>
)

object SmartVoiceParser {

    private val NUMBER_MAP = mapOf(
        "ek" to "1", "one" to "1", "1" to "1", "एक" to "1", "१" to "1",
        "do" to "2", "two" to "2", "2" to "2", "दो" to "2", "२" to "2",
        "teen" to "3", "three" to "3", "3" to "3", "तीन" to "3", "३" to "3",
        "char" to "4", "four" to "4", "4" to "4", "चार" to "4", "४" to "4",
        "paanch" to "5", "five" to "5", "5" to "5", "पांच" to "5", "पाँच" to "5", "५" to "5",
        "chhah" to "6", "six" to "6", "6" to "6", "छह" to "6", "६" to "6",
        "saat" to "7", "seven" to "7", "7" to "7", "सात" to "7",
        "aath" to "8", "eight" to "8", "8" to "8", "आठ" to "8",
        "nau" to "9", "nine" to "9", "9" to "9", "नौ" to "9",
        "das" to "10", "ten" to "10", "10" to "10", "दस" to "10",
        "aadha" to "500g", "half" to "500g", "आधा" to "500g",
        "paanv" to "250g", "pav" to "250g", "पाव" to "250g",
        "dedh" to "1.5 kg", "डेढ़" to "1.5 kg",
        "dhai" to "2.5 kg", "ढाई" to "2.5 kg"
    )

    private val UNIT_MAP = mapOf(
        "kilo" to "kg", "kg" to "kg", "kgs" to "kg", "kilogram" to "kg", "किलो" to "kg", "किग्रा" to "kg",
        "gram" to "g", "gm" to "g", "g" to "g", "grams" to "g", "ग्राम" to "g",
        "packet" to "Packet", "packets" to "Packet", "pkt" to "Packet", "pack" to "Packet", "पैकेट" to "Packet",
        "piece" to "Piece", "pieces" to "Piece", "pc" to "Piece", "pcs" to "Piece", "पीस" to "Piece", "नग" to "Piece",
        "liter" to "L", "litre" to "L", "litres" to "L", "liters" to "L", "l" to "L", "लीटर" to "L",
        "darjan" to "Dozen", "dozen" to "Dozen", "दर्जन" to "Dozen",
        "roll" to "Roll", "रोल" to "Roll",
        "refill" to "Refill", "रिफिल" to "Refill"
    )

    /**
     * Parses spoken user text and matches with known grocery items
     */
    fun parseSpokenInput(spokenText: String, allItems: List<GroceryItemEntity>): VoiceParseResult {
        if (spokenText.isBlank()) {
            return VoiceParseResult("", emptyList(), emptyList())
        }

        // Split speech on conjunctions: aur, and, tatha, comma, plus, sath me, bhi
        val normalized = spokenText
            .replace(" तथा ", " aur ")
            .replace(" और ", " aur ")
            .replace(" and ", " aur ")
            .replace(" plus ", " aur ")
            .replace(" sath me ", " aur ")
            .replace(" साथ में ", " aur ")
            .replace(" साथ मे ", " aur ")
            .replace(" भी ", " aur ")
            .replace(" एवं ", " aur ")
            .replace(",", " aur ")
            .replace("।", " aur ")

        val segments = normalized.split(" aur ").map { it.trim() }.filter { it.isNotBlank() }
        val matchedResults = mutableListOf<ParsedVoiceItem>()
        val alreadyMatchedIds = mutableSetOf<Int>()

        for (segment in segments) {
            val words = segment.lowercase(Locale.ROOT).split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (words.isEmpty()) continue

            // 1. Extract quantity and unit if present
            var quantityNumber: String? = null
            var quantityUnit: String? = null

            for (i in words.indices) {
                val word = words[i]
                if (NUMBER_MAP.containsKey(word)) {
                    val mappedVal = NUMBER_MAP[word]!!
                    if (mappedVal.contains("kg") || mappedVal.contains("g")) {
                        // Already compound like "aadha" -> "500g"
                        quantityNumber = mappedVal
                    } else {
                        quantityNumber = mappedVal
                    }
                } else if (word.matches("^\\d+(\\.\\d+)?$".toRegex())) {
                    quantityNumber = word
                }

                if (UNIT_MAP.containsKey(word)) {
                    quantityUnit = UNIT_MAP[word]
                }
            }

            // 2. Find best matching grocery item
            var bestItem: GroceryItemEntity? = null
            var highestScore = 0

            for (item in allItems) {
                if (alreadyMatchedIds.contains(item.id)) continue

                var score = 0
                val searchKeys = (item.keywords + "," + item.nameEnglish + "," + item.nameHindi)
                    .lowercase(Locale.ROOT)
                    .split(",")
                    .map { it.trim() }

                for (key in searchKeys) {
                    if (key.isBlank()) continue
                    // Exact phrase match in segment
                    if (segment.lowercase(Locale.ROOT).contains(key)) {
                        score = maxOf(score, key.length * 2 + 10)
                    } else {
                        // Token match
                        for (w in words) {
                            if (w.length >= 3 && key.contains(w)) {
                                score = maxOf(score, w.length)
                            }
                        }
                    }
                }

                if (score > highestScore && score >= 4) {
                    highestScore = score
                    bestItem = item
                }
            }

            if (bestItem != null) {
                alreadyMatchedIds.add(bestItem.id)

                // Build finalized quantity
                val finalQuantity = when {
                    quantityNumber != null && quantityNumber.contains(" ") -> quantityNumber
                    quantityNumber != null && (quantityNumber.endsWith("g") || quantityNumber.endsWith("kg")) -> quantityNumber
                    quantityNumber != null && quantityUnit != null -> "$quantityNumber $quantityUnit"
                    quantityNumber != null -> {
                        // Guess unit from item category or default
                        val defaultUnit = bestItem.quantity.substringAfter(" ", "kg")
                        "$quantityNumber $defaultUnit"
                    }
                    quantityUnit != null -> "1 $quantityUnit"
                    else -> bestItem.quantity // fallback to item's default preset
                }

                matchedResults.add(
                    ParsedVoiceItem(
                        matchedItem = bestItem,
                        parsedQuantity = finalQuantity,
                        rawMatchedPhrase = segment
                    )
                )
            }
        }

        return VoiceParseResult(
            rawText = spokenText,
            matchedItems = matchedResults,
            unrecognizedTokens = emptyList()
        )
    }
}
