package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import com.example.data.model.Language
import com.example.data.repository.GroceryRepository
import com.example.voice.SmartVoiceParser
import com.example.voice.VoiceSpeechManager
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroceryViewModel(
    private val repository: GroceryRepository,
    val voiceSpeechManager: VoiceSpeechManager
) : ViewModel() {

    private val _language = MutableStateFlow(Language.HINDI)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _selectedCategory = MutableStateFlow<GroceryCategory?>(null)
    val selectedCategory: StateFlow<GroceryCategory?> = _selectedCategory.asStateFlow()

    private val _showOnlySelected = MutableStateFlow(false)
    val showOnlySelected: StateFlow<Boolean> = _showOnlySelected.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allItems: StateFlow<List<GroceryItemEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedItems: StateFlow<List<GroceryItemEntity>> = repository.selectedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isVoiceAssistantOpen = MutableStateFlow(false)
    val isVoiceAssistantOpen: StateFlow<Boolean> = _isVoiceAssistantOpen.asStateFlow()

    private val _isReviewSheetOpen = MutableStateFlow(false)
    val isReviewSheetOpen: StateFlow<Boolean> = _isReviewSheetOpen.asStateFlow()

    private val _isAddItemDialogOpen = MutableStateFlow(false)
    val isAddItemDialogOpen: StateFlow<Boolean> = _isAddItemDialogOpen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _lastVoiceFeedback = MutableStateFlow<String?>(null)
    val lastVoiceFeedback: StateFlow<String?> = _lastVoiceFeedback.asStateFlow()

    val filteredItems: StateFlow<List<GroceryItemEntity>> = combine(
        allItems,
        _selectedCategory,
        _showOnlySelected,
        _searchQuery
    ) { items, cat, onlySelected, query ->
        items.filter { item ->
            val matchesCategory = cat == null || item.category == cat.id
            val matchesSelected = !onlySelected || item.isSelected
            val matchesQuery = query.isBlank() ||
                item.nameHindi.contains(query, ignoreCase = true) ||
                item.nameEnglish.contains(query, ignoreCase = true) ||
                item.keywords.contains(query, ignoreCase = true)

            matchesCategory && matchesSelected && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        voiceSpeechManager.onSpeechRecognized = { spokenText ->
            handleSpokenText(spokenText)
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == Language.HINDI) Language.ENGLISH else Language.HINDI
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
    }

    fun setCategory(category: GroceryCategory?) {
        _selectedCategory.value = category
    }

    fun setShowOnlySelected(onlySelected: Boolean) {
        _showOnlySelected.value = onlySelected
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleItemSelection(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.toggleSelection(item.id, !item.isSelected)
        }
    }

    fun updateItemQuantity(item: GroceryItemEntity, newQuantity: String) {
        viewModelScope.launch {
            repository.updateQuantity(item.id, newQuantity)
        }
    }

    fun selectAllInCategory(category: GroceryCategory, selectAll: Boolean) {
        viewModelScope.launch {
            repository.selectAllInCategory(category.id, selectAll)
        }
    }

    fun clearAllSelected() {
        viewModelScope.launch {
            repository.clearAllSelected()
            val msg = if (_language.value == Language.HINDI) "सभी चुने गए सामान हटा दिए गए" else "All selected items cleared"
            _snackbarMessage.value = msg
        }
    }

    fun addNewCustomItem(
        nameHindi: String,
        nameEnglish: String,
        category: GroceryCategory,
        iconEmoji: String,
        quantity: String
    ) {
        viewModelScope.launch {
            val item = GroceryItemEntity(
                nameHindi = nameHindi.ifBlank { nameEnglish },
                nameEnglish = nameEnglish.ifBlank { nameHindi },
                category = category.id,
                iconEmoji = iconEmoji.ifBlank { "🛍️" },
                isSelected = true,
                quantity = quantity.ifBlank { "1 Piece" },
                availableUnits = "1 Piece,1 Packet,500g,1 kg,2 kg",
                keywords = "${nameHindi.lowercase()}, ${nameEnglish.lowercase()}",
                isCustom = true
            )
            repository.insertItem(item)
            _isAddItemDialogOpen.value = false
            val msg = if (_language.value == Language.HINDI)
                "\"${item.nameHindi}\" लिस्ट में जोड़ा गया"
            else
                "\"${item.nameEnglish}\" added to list"
            _snackbarMessage.value = msg
        }
    }

    fun deleteItem(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun openVoiceAssistant() {
        _isVoiceAssistantOpen.value = true
        _lastVoiceFeedback.value = null
        voiceSpeechManager.startListening(_language.value)
    }

    fun closeVoiceAssistant() {
        _isVoiceAssistantOpen.value = false
        voiceSpeechManager.resetState()
    }

    fun openReviewSheet() {
        _isReviewSheetOpen.value = true
    }

    fun closeReviewSheet() {
        _isReviewSheetOpen.value = false
    }

    fun openAddItemDialog() {
        _isAddItemDialogOpen.value = true
    }

    fun closeAddItemDialog() {
        _isAddItemDialogOpen.value = false
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun handleSpokenText(spokenPhrase: String) {
        viewModelScope.launch {
            val parseResult = SmartVoiceParser.parseSpokenInput(spokenPhrase, allItems.value)
            if (parseResult.matchedItems.isNotEmpty()) {
                val matchedNames = mutableListOf<String>()
                for (matched in parseResult.matchedItems) {
                    repository.toggleSelection(matched.matchedItem.id, true)
                    repository.updateQuantity(matched.matchedItem.id, matched.parsedQuantity)
                    val displayName = matched.matchedItem.getName(_language.value)
                    matchedNames.add("$displayName ${matched.parsedQuantity}")
                }

                val confirmationText = if (_language.value == Language.HINDI) {
                    "${matchedNames.joinToString(", ")} लिस्ट में जोड़ दिए गए हैं"
                } else {
                    "Added to list: ${matchedNames.joinToString(", ")}"
                }

                _lastVoiceFeedback.value = confirmationText
                _snackbarMessage.value = confirmationText
                voiceSpeechManager.speak(confirmationText, _language.value)
            } else {
                val notFoundText = if (_language.value == Language.HINDI) {
                    "\"$spokenPhrase\" लिस्ट में नहीं मिला। कृपया नीचे से चुनें या नया सामान जोड़ें।"
                } else {
                    "Could not match \"$spokenPhrase\". Tap 'Add Custom Item' or pick from list."
                }
                _lastVoiceFeedback.value = notFoundText
                _snackbarMessage.value = notFoundText
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSpeechManager.release()
    }
}

class GroceryViewModelFactory(
    private val repository: GroceryRepository,
    private val voiceSpeechManager: VoiceSpeechManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            return GroceryViewModel(repository, voiceSpeechManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
