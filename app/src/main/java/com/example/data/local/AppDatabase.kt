package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [GroceryItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghar_ki_list_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.groceryDao())
                }
            }
        }

        suspend fun populateInitialData(dao: GroceryDao) {
            val initialItems = listOf(
                // 1. Sabzi & Phal (Vegetables & Fruits)
                GroceryItemEntity(
                    nameHindi = "आलू",
                    nameEnglish = "Potato (Aloo)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🥔",
                    quantity = "2 kg",
                    availableUnits = "1 kg,2 kg,5 kg,500g",
                    keywords = "aloo,alu,patata,batata,potato,आलू",
                    orderIndex = 1
                ),
                GroceryItemEntity(
                    nameHindi = "प्याज",
                    nameEnglish = "Onion (Pyaj)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🧅",
                    quantity = "1 kg",
                    availableUnits = "1 kg,2 kg,5 kg,500g",
                    keywords = "onion,pyaj,pyaaj,kanda,प्याज,प्याज़",
                    orderIndex = 2
                ),
                GroceryItemEntity(
                    nameHindi = "टमाटर",
                    nameEnglish = "Tomato (Tamatar)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🍅",
                    quantity = "1 kg",
                    availableUnits = "500g,1 kg,2 kg",
                    keywords = "tomato,tamatar,tamater,टमाटर",
                    orderIndex = 3
                ),
                GroceryItemEntity(
                    nameHindi = "अदरक",
                    nameEnglish = "Ginger (Adrak)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🫚",
                    quantity = "250g",
                    availableUnits = "100g,250g,500g",
                    keywords = "ginger,adrak,adrakh,अदरक",
                    orderIndex = 4
                ),
                GroceryItemEntity(
                    nameHindi = "लहसुन",
                    nameEnglish = "Garlic (Lehsun)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🧄",
                    quantity = "250g",
                    availableUnits = "100g,250g,500g",
                    keywords = "garlic,lehsun,lasan,लहसुन",
                    orderIndex = 5
                ),
                GroceryItemEntity(
                    nameHindi = "हरी मिर्च",
                    nameEnglish = "Green Chilli (Hari Mirch)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🌶️",
                    quantity = "250g",
                    availableUnits = "100g,250g,500g",
                    keywords = "chilli,green chilli,hari mirch,mirchi,मिर्च,हरी मिर्च",
                    orderIndex = 6
                ),
                GroceryItemEntity(
                    nameHindi = "हरा धनिया",
                    nameEnglish = "Coriander (Dhaniya)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🌿",
                    quantity = "1 Piece",
                    availableUnits = "1 Piece,2 Piece,100g,250g",
                    keywords = "coriander,dhaniya,hara dhaniya,धनिया",
                    orderIndex = 7
                ),
                GroceryItemEntity(
                    nameHindi = "नींबू",
                    nameEnglish = "Lemon (Nimbu)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🍋",
                    quantity = "4 Piece",
                    availableUnits = "2 Piece,4 Piece,6 Piece,500g",
                    keywords = "lemon,nimbu,neembu,नींबू",
                    orderIndex = 8
                ),
                GroceryItemEntity(
                    nameHindi = "पालक",
                    nameEnglish = "Spinach (Palak)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🥬",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,1 Piece",
                    keywords = "spinach,palak,paalak,पालक",
                    orderIndex = 9
                ),
                GroceryItemEntity(
                    nameHindi = "फूलगोभी / पत्तागोभी",
                    nameEnglish = "Cauliflower / Cabbage",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🥦",
                    quantity = "1 Piece",
                    availableUnits = "1 Piece,2 Piece,1 kg",
                    keywords = "gobi,gobhi,cauliflower,cabbage,phool gobi,गोभी,फूलगोभी",
                    orderIndex = 10
                ),
                GroceryItemEntity(
                    nameHindi = "भिंडी",
                    nameEnglish = "Lady Finger (Bhindi)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🥗",
                    quantity = "500g",
                    availableUnits = "500g,1 kg",
                    keywords = "bhindi,okra,ladyfinger,भिंडी",
                    orderIndex = 11
                ),
                GroceryItemEntity(
                    nameHindi = "केला",
                    nameEnglish = "Banana (Kela)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🍌",
                    quantity = "1 Dozen",
                    availableUnits = "6 Piece,1 Dozen,2 Dozen",
                    keywords = "banana,kela,केला,दर्जन केला",
                    orderIndex = 12
                ),
                GroceryItemEntity(
                    nameHindi = "सेब",
                    nameEnglish = "Apple (Seb)",
                    category = GroceryCategory.VEGETABLES_FRUITS.id,
                    iconEmoji = "🍎",
                    quantity = "1 kg",
                    availableUnits = "500g,1 kg,2 kg",
                    keywords = "apple,seb,saib,सेब",
                    orderIndex = 13
                ),

                // 2. Daal & Anaj (Pulses & Grains)
                GroceryItemEntity(
                    nameHindi = "गेहूं का आटा",
                    nameEnglish = "Wheat Flour (Atta)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🌾",
                    quantity = "5 kg",
                    availableUnits = "1 kg,2 kg,5 kg,10 kg",
                    keywords = "atta,aata,wheat flour,flour,आटा,गेहूं का आटा",
                    orderIndex = 1
                ),
                GroceryItemEntity(
                    nameHindi = "चावल",
                    nameEnglish = "Rice (Chawal)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🍚",
                    quantity = "2 kg",
                    availableUnits = "1 kg,2 kg,5 kg,10 kg",
                    keywords = "rice,chawal,basmati,chaawal,चावल",
                    orderIndex = 2
                ),
                GroceryItemEntity(
                    nameHindi = "अरहर / तूर दाल",
                    nameEnglish = "Toor Dal (Arhar)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🥣",
                    quantity = "1 kg",
                    availableUnits = "500g,1 kg,2 kg",
                    keywords = "toor dal,arhar dal,tuvar,dal,तूर दाल,अरहर दाल,दाल",
                    orderIndex = 3
                ),
                GroceryItemEntity(
                    nameHindi = "मूंग दाल",
                    nameEnglish = "Moong Dal",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🥣",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,2 kg",
                    keywords = "moong dal,mung dal,मूंग दाल",
                    orderIndex = 4
                ),
                GroceryItemEntity(
                    nameHindi = "चना दाल",
                    nameEnglish = "Chana Dal",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🥣",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,2 kg",
                    keywords = "chana dal,channa dal,चना दाल",
                    orderIndex = 5
                ),
                GroceryItemEntity(
                    nameHindi = "राजमा / छोले",
                    nameEnglish = "Rajma / Chickpeas (Chhole)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🫘",
                    quantity = "500g",
                    availableUnits = "500g,1 kg",
                    keywords = "rajma,chole,chhole,kabuli chana,rajmah,राजमा,छोले",
                    orderIndex = 6
                ),
                GroceryItemEntity(
                    nameHindi = "बेसन",
                    nameEnglish = "Gram Flour (Besan)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🟡",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,1 Packet",
                    keywords = "besan,baisan,gram flour,बेसन",
                    orderIndex = 7
                ),
                GroceryItemEntity(
                    nameHindi = "सूजी / रवा",
                    nameEnglish = "Semolina (Suji / Rava)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🌾",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,1 Packet",
                    keywords = "suji,sooji,rava,सूजी,रवा",
                    orderIndex = 8
                ),
                GroceryItemEntity(
                    nameHindi = "पोहा",
                    nameEnglish = "Flattened Rice (Poha)",
                    category = GroceryCategory.GRAINS_PULSES.id,
                    iconEmoji = "🥣",
                    quantity = "500g",
                    availableUnits = "500g,1 kg,1 Packet",
                    keywords = "poha,powa,pohe,पोहा",
                    orderIndex = 9
                ),

                // 3. Masale & Grocery (Spices & Pantry)
                GroceryItemEntity(
                    nameHindi = "हल्दी पाउडर",
                    nameEnglish = "Turmeric Powder (Haldi)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🟡",
                    quantity = "200g",
                    availableUnits = "100g,200g,500g,1 Packet",
                    keywords = "haldi,turmeric,haldi powder,हल्दी",
                    orderIndex = 1
                ),
                GroceryItemEntity(
                    nameHindi = "लाल मिर्च पाउडर",
                    nameEnglish = "Red Chilli Powder",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🌶️",
                    quantity = "200g",
                    availableUnits = "100g,200g,500g,1 Packet",
                    keywords = "lal mirch,red chilli,mirchi powder,लाल मिर्च",
                    orderIndex = 2
                ),
                GroceryItemEntity(
                    nameHindi = "धनिया पाउडर",
                    nameEnglish = "Coriander Powder",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🟤",
                    quantity = "200g",
                    availableUnits = "100g,200g,500g,1 Packet",
                    keywords = "dhaniya powder,coriander powder,धनिया पाउडर",
                    orderIndex = 3
                ),
                GroceryItemEntity(
                    nameHindi = "गरम मसाला",
                    nameEnglish = "Garam Masala",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🧂",
                    quantity = "100g",
                    availableUnits = "50g,100g,1 Packet",
                    keywords = "garam masala,masala,गरम मसाला",
                    orderIndex = 4
                ),
                GroceryItemEntity(
                    nameHindi = "जीरा",
                    nameEnglish = "Cumin Seeds (Jeera)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🌾",
                    quantity = "100g",
                    availableUnits = "100g,250g,500g",
                    keywords = "jeera,jira,cumin,जीरा",
                    orderIndex = 5
                ),
                GroceryItemEntity(
                    nameHindi = "नमक",
                    nameEnglish = "Salt (Namak)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🧂",
                    quantity = "1 Packet",
                    availableUnits = "1 Packet,2 Packet,1 kg",
                    keywords = "salt,namak,tata salt,नमक",
                    orderIndex = 6
                ),
                GroceryItemEntity(
                    nameHindi = "चीनी",
                    nameEnglish = "Sugar (Chini)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🍬",
                    quantity = "1 kg",
                    availableUnits = "1 kg,2 kg,5 kg",
                    keywords = "sugar,chini,cheeni,shakkar,चीनी",
                    orderIndex = 7
                ),
                GroceryItemEntity(
                    nameHindi = "चाय पत्ती",
                    nameEnglish = "Tea Leaves (Chai)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🍵",
                    quantity = "500g",
                    availableUnits = "250g,500g,1 kg,1 Packet",
                    keywords = "chai,tea,chai patti,tea leaves,चाय,चाय पत्ती",
                    orderIndex = 8
                ),
                GroceryItemEntity(
                    nameHindi = "सरसों / रिफाइंड तेल",
                    nameEnglish = "Cooking Oil (Tel)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🫗",
                    quantity = "1 L",
                    availableUnits = "1 L,2 L,5 L,1 Packet",
                    keywords = "oil,tel,sarson tel,mustard oil,refined oil,cooking oil,तेल",
                    orderIndex = 9
                ),
                GroceryItemEntity(
                    nameHindi = "देसी घी",
                    nameEnglish = "Pure Desi Ghee",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🧈",
                    quantity = "1 L",
                    availableUnits = "500g,1 L,1 kg",
                    keywords = "ghee,desi ghee,amul ghee,घी,देसी घी",
                    orderIndex = 10
                ),
                GroceryItemEntity(
                    nameHindi = "हींग",
                    nameEnglish = "Asafoetida (Hing)",
                    category = GroceryCategory.SPICES_PANTRY.id,
                    iconEmoji = "🧂",
                    quantity = "1 Piece",
                    availableUnits = "1 Piece,1 Packet",
                    keywords = "hing,asafoetida,हींग",
                    orderIndex = 11
                ),

                // 4. Doodh & Dairy (Milk & Dairy)
                GroceryItemEntity(
                    nameHindi = "दूध",
                    nameEnglish = "Fresh Milk (Doodh)",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🥛",
                    quantity = "1 L",
                    availableUnits = "500 ml,1 L,2 L,1 Packet,2 Packet",
                    keywords = "milk,doodh,dudh,amul milk,mother dairy,दूध",
                    orderIndex = 1
                ),
                GroceryItemEntity(
                    nameHindi = "दही",
                    nameEnglish = "Curd / Dahi",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🥣",
                    quantity = "1 Packet",
                    availableUnits = "1 Packet,2 Packet,500g,1 kg",
                    keywords = "curd,dahi,yogurt,दही",
                    orderIndex = 2
                ),
                GroceryItemEntity(
                    nameHindi = "पनीर",
                    nameEnglish = "Paneer (Cottage Cheese)",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🧀",
                    quantity = "250g",
                    availableUnits = "200g,250g,500g,1 Packet",
                    keywords = "paneer,panir,cottage cheese,पनीर",
                    orderIndex = 3
                ),
                GroceryItemEntity(
                    nameHindi = "मक्खन",
                    nameEnglish = "Butter (Makhan)",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🧈",
                    quantity = "100g",
                    availableUnits = "100g,500g,1 Packet",
                    keywords = "butter,makhan,amul butter,मक्खन",
                    orderIndex = 4
                ),
                GroceryItemEntity(
                    nameHindi = "ब्रेड",
                    nameEnglish = "Bread Packet",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🍞",
                    quantity = "1 Packet",
                    availableUnits = "1 Packet,2 Packet",
                    keywords = "bread,brown bread,white bread,ब्रेड",
                    orderIndex = 5
                ),
                GroceryItemEntity(
                    nameHindi = "अंडे",
                    nameEnglish = "Eggs (Ande)",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🥚",
                    quantity = "6 Piece",
                    availableUnits = "6 Piece,12 Piece,1 Tray (30)",
                    keywords = "egg,eggs,ande,anda,अंडे,अंडा",
                    orderIndex = 6
                ),
                GroceryItemEntity(
                    nameHindi = "चीज़ स्लाइस",
                    nameEnglish = "Cheese Slices / Cube",
                    category = GroceryCategory.DAIRY.id,
                    iconEmoji = "🧀",
                    quantity = "1 Packet",
                    availableUnits = "1 Packet,2 Packet",
                    keywords = "cheese,amul cheese,cheese slice,चीज़",
                    orderIndex = 7
                ),

                // 5. Ghar ki Safai (Cleaning & Household)
                GroceryItemEntity(
                    nameHindi = "कपड़े धोने का पाउडर / सर्फ",
                    nameEnglish = "Washing Powder (Surf)",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🧼",
                    quantity = "1 kg",
                    availableUnits = "1 kg,2 kg,5 kg,1 Packet,2 Packet",
                    keywords = "surf,washing powder,detergent,surf excel,tide,aerial,सर्फ,डिटर्जेंट",
                    orderIndex = 1
                ),
                GroceryItemEntity(
                    nameHindi = "बर्तन धोने का साबुन / विम",
                    nameEnglish = "Dishwash Bar / Vim",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🧽",
                    quantity = "2 Piece",
                    availableUnits = "1 Piece,2 Piece,3 Piece,1 Packet",
                    keywords = "vim,dishwash,vim bar,pril,bartan sabun,विम,बर्तन साबुन",
                    orderIndex = 2
                ),
                GroceryItemEntity(
                    nameHindi = "फर्श क्लीनर / लाइजोल",
                    nameEnglish = "Floor Cleaner (Lizol)",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🧴",
                    quantity = "1 L",
                    availableUnits = "500 ml,1 L,2 L,1 Piece",
                    keywords = "lizol,floor cleaner,pocha liquid,phenyl,फर्श क्लीनर,लाइजोल",
                    orderIndex = 3
                ),
                GroceryItemEntity(
                    nameHindi = "टॉयलेट क्लीनर / हार्पिक",
                    nameEnglish = "Toilet Cleaner (Harpic)",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🚽",
                    quantity = "1 Piece",
                    availableUnits = "500 ml,1 L,1 Piece",
                    keywords = "harpic,toilet cleaner,हार्पिक",
                    orderIndex = 4
                ),
                GroceryItemEntity(
                    nameHindi = "झाड़ू / पोंछा",
                    nameEnglish = "Broom & Mop (Jhadu)",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🧹",
                    quantity = "1 Piece",
                    availableUnits = "1 Piece,2 Piece",
                    keywords = "jhadu,broom,mop,pocha,झाड़ू,पोंछा",
                    orderIndex = 5
                ),
                GroceryItemEntity(
                    nameHindi = "नहाने का साबुन",
                    nameEnglish = "Bath Soap",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🧼",
                    quantity = "1 Packet (4 pcs)",
                    availableUnits = "1 Piece,1 Packet (4 pcs),2 Packet",
                    keywords = "soap,bath soap,dettol,lux,lifebuoy,dove,साबुन",
                    orderIndex = 6
                ),
                GroceryItemEntity(
                    nameHindi = "टूथपेस्ट",
                    nameEnglish = "Toothpaste",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🪥",
                    quantity = "1 Piece",
                    availableUnits = "1 Piece,2 Piece,100g,150g",
                    keywords = "toothpaste,colgate,pepsodent,dabur red,toot paste,टूथपेस्ट",
                    orderIndex = 7
                ),
                GroceryItemEntity(
                    nameHindi = "मच्छर अगरबत्ती / ऑल आउट",
                    nameEnglish = "Mosquito Repellent (All Out)",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🦟",
                    quantity = "1 Refill",
                    availableUnits = "1 Refill,2 Refill,1 Packet",
                    keywords = "all out,good knight,mosquito,agarbatti,मच्छर",
                    orderIndex = 8
                ),
                GroceryItemEntity(
                    nameHindi = "कूड़े की थैली",
                    nameEnglish = "Garbage Bags",
                    category = GroceryCategory.CLEANING_HOUSEHOLD.id,
                    iconEmoji = "🗑️",
                    quantity = "1 Roll",
                    availableUnits = "1 Roll,2 Roll,1 Packet",
                    keywords = "garbage bag,dustbin bag,trash bag,कूड़े की थैली",
                    orderIndex = 9
                )
            )
            dao.insertAll(initialItems)
        }
    }
}
