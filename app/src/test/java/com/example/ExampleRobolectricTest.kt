package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import com.example.data.model.Language
import com.example.voice.SmartVoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun read_string_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ghar Ki List", appName)
    }

    @Test
    fun test_smart_voice_parser_hindi_and_english() {
        val testItems = listOf(
            GroceryItemEntity(
                id = 1,
                nameHindi = "आलू",
                nameEnglish = "Potato (Aloo)",
                category = GroceryCategory.VEGETABLES_FRUITS.id,
                iconEmoji = "🥔",
                quantity = "1 kg",
                keywords = "aloo,alu,potato,आलू"
            ),
            GroceryItemEntity(
                id = 2,
                nameHindi = "दूध",
                nameEnglish = "Fresh Milk (Doodh)",
                category = GroceryCategory.DAIRY.id,
                iconEmoji = "🥛",
                quantity = "1 L",
                keywords = "milk,doodh,dudh,दूध"
            ),
            GroceryItemEntity(
                id = 3,
                nameHindi = "टमाटर",
                nameEnglish = "Tomato (Tamatar)",
                category = GroceryCategory.VEGETABLES_FRUITS.id,
                iconEmoji = "🍅",
                quantity = "1 kg",
                keywords = "tomato,tamatar,टमाटर"
            )
        )

        // Test Phrase 1: "2 kilo aloo aur 1 packet doodh"
        val result1 = SmartVoiceParser.parseSpokenInput("2 kilo aloo aur 1 packet doodh", testItems)
        assertEquals(2, result1.matchedItems.size)
        val alooMatch = result1.matchedItems.find { it.matchedItem.id == 1 }
        val doodhMatch = result1.matchedItems.find { it.matchedItem.id == 2 }

        assertNotNull(alooMatch)
        assertEquals("2 kg", alooMatch?.parsedQuantity)

        assertNotNull(doodhMatch)
        assertEquals("1 Packet", doodhMatch?.parsedQuantity)

        // Test Phrase 2: "दो किलो टमाटर" (Devanagari Hindi)
        val result2 = SmartVoiceParser.parseSpokenInput("दो किलो टमाटर", testItems)
        assertEquals(1, result2.matchedItems.size)
        val tamatarMatch = result2.matchedItems.first()
        assertEquals(3, tamatarMatch.matchedItem.id)
        assertEquals("2 kg", tamatarMatch.parsedQuantity)
    }

    @Test
    fun test_category_bilingual_names() {
        val vegCat = GroceryCategory.VEGETABLES_FRUITS
        assertEquals("सब्ज़ी और फल", vegCat.getDisplayName(Language.HINDI))
        assertEquals("Sabzi & Phal", vegCat.getDisplayName(Language.ENGLISH))
    }
}
