package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GroceryCategory
import com.example.data.model.Language
import com.example.pdf.PdfShareManager
import com.example.ui.components.*
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenContainer
import com.example.ui.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryHomeScreen(
    viewModel: GroceryViewModel
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showOnlySelected by viewModel.showOnlySelected.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val isVoiceAssistantOpen by viewModel.isVoiceAssistantOpen.collectAsStateWithLifecycle()
    val isReviewSheetOpen by viewModel.isReviewSheetOpen.collectAsStateWithLifecycle()
    val isAddItemDialogOpen by viewModel.isAddItemDialogOpen.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val lastVoiceFeedback by viewModel.lastVoiceFeedback.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Audio record permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openVoiceAssistant()
        } else {
            // Still open voice modal for text/preset simulation
            viewModel.openVoiceAssistant()
        }
    }

    fun requestVoiceSearch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openVoiceAssistant()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("grocery_home_screen"),
        topBar = {
            TopBarWithLanguageToggle(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                selectedCount = selectedItems.size,
                onClearAll = { viewModel.clearAllSelected() },
                onOpenAddCustomItem = { viewModel.openAddItemDialog() },
                onOpenVoiceSearch = { requestVoiceSearch() }
            )
        },
        bottomBar = {
            StickyBottomActionBar(
                selectedCount = selectedItems.size,
                language = language,
                onOpenVoiceAssistant = { requestVoiceSearch() },
                onOpenReview = { viewModel.openReviewSheet() },
                onDirectSharePdf = {
                    if (selectedItems.isEmpty()) {
                        val msg = if (language == Language.HINDI) "कृपया पहले लिस्ट में से सामान चुनें!" else "Please select items first!"
                        viewModel.showSnackbar(msg)
                    } else {
                        val pdfFile = PdfShareManager.generateGroceryPdf(
                            context = context,
                            selectedItems = selectedItems,
                            language = language
                        )
                        if (pdfFile != null) {
                            PdfShareManager.sharePdfFile(context, pdfFile, language)
                        } else {
                            val textMsg = PdfShareManager.formatWhatsAppMessage(
                                items = selectedItems,
                                language = language
                            )
                            PdfShareManager.shareTextToWhatsApp(context, textMsg, language)
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 70.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Horizontal Scroll Category Tabs
            CategoryScrollTabs(
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.setCategory(it) },
                showOnlySelected = showOnlySelected,
                onToggleShowOnlySelected = { viewModel.setShowOnlySelected(it) },
                allItems = allItems,
                selectedItems = selectedItems,
                language = language
            )

            // 2. Category Quick Action Sub-bar (e.g. Select All / Info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header info
                val categoryTitle = when {
                    showOnlySelected -> if (language == Language.HINDI) "✅ चुने गए सामान (${filteredItems.size})" else "✅ Selected Items (${filteredItems.size})"
                    selectedCategory != null -> "${selectedCategory!!.emoji} ${selectedCategory!!.getDisplayName(language)} (${filteredItems.size})"
                    searchQuery.isNotBlank() -> if (language == Language.HINDI) "🔍 खोज परिणाम (${filteredItems.size})" else "🔍 Search Results (${filteredItems.size})"
                    else -> if (language == Language.HINDI) "🛒 सभी घरेलू सामान (${filteredItems.size})" else "🛒 All Household Items (${filteredItems.size})"
                }

                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Select All in Category toggle button
                if (selectedCategory != null && !showOnlySelected && filteredItems.isNotEmpty()) {
                    val allInCatSelected = filteredItems.all { it.isSelected }
                    TextButton(
                        onClick = {
                            viewModel.selectAllInCategory(selectedCategory!!, !allInCatSelected)
                        },
                        modifier = Modifier.testTag("select_all_category_button")
                    ) {
                        Text(
                            text = if (allInCatSelected) {
                                if (language == Language.HINDI) "सब हटाएं" else "Unselect All"
                            } else {
                                if (language == Language.HINDI) "सब चुनें" else "Select All"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            // 3. Items List / Empty State
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (showOnlySelected) {
                                if (language == Language.HINDI) "अभी तक कोई सामान नहीं चुना गया है।" else "No items selected yet."
                            } else {
                                if (language == Language.HINDI) "कोई सामान नहीं मिला।" else "No matching items found."
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.HINDI) "माइक दबाकर बोलें या नया सामान जोड़ें।" else "Use voice search or tap '+' to add a custom item.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.openAddItemDialog() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == Language.HINDI) "नया सामान जोड़ें" else "Add Item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 12.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id }
                    ) { item ->
                        GroceryItemCard(
                            item = item,
                            language = language,
                            onToggleSelect = { viewModel.toggleItemSelection(item) },
                            onUpdateQuantity = { newQty -> viewModel.updateItemQuantity(item, newQty) },
                            onDeleteItem = if (item.isCustom) {
                                { viewModel.deleteItem(item) }
                            } else null
                        )
                    }
                }
            }
        }
    }

    // Voice Assistant Bottom Sheet
    if (isVoiceAssistantOpen) {
        VoiceAssistantBottomSheet(
            language = language,
            voiceSpeechManager = viewModel.voiceSpeechManager,
            lastVoiceFeedback = lastVoiceFeedback,
            onDismiss = { viewModel.closeVoiceAssistant() },
            onSimulatePhrase = { phrase ->
                viewModel.handleSpokenText(phrase)
            }
        )
    }

    // Send List / PDF Modal Sheet
    if (isReviewSheetOpen) {
        SendListModalSheet(
            selectedItems = selectedItems,
            language = language,
            onDismiss = { viewModel.closeReviewSheet() }
        )
    }

    // Add Custom Item Dialog
    if (isAddItemDialogOpen) {
        AddCustomItemDialog(
            language = language,
            onDismiss = { viewModel.closeAddItemDialog() },
            onAddItem = { hindi, eng, cat, emoji, qty ->
                viewModel.addNewCustomItem(hindi, eng, cat, emoji, qty)
            }
        )
    }
}
