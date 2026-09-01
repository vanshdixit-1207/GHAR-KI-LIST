package com.example.data.repository

import com.example.data.local.GroceryDao
import com.example.data.model.GroceryItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GroceryRepository(private val groceryDao: GroceryDao) {

    val allItems: Flow<List<GroceryItemEntity>> = groceryDao.getAllItems()
    val selectedItems: Flow<List<GroceryItemEntity>> = groceryDao.getSelectedItems()

    fun getItemsByCategory(category: String): Flow<List<GroceryItemEntity>> {
        return groceryDao.getItemsByCategory(category)
    }

    suspend fun toggleSelection(id: Int, isSelected: Boolean) = withContext(Dispatchers.IO) {
        groceryDao.toggleSelection(id, isSelected)
    }

    suspend fun updateQuantity(id: Int, quantity: String) = withContext(Dispatchers.IO) {
        groceryDao.updateQuantity(id, quantity)
    }

    suspend fun insertItem(item: GroceryItemEntity): Long = withContext(Dispatchers.IO) {
        groceryDao.insertItem(item)
    }

    suspend fun updateItem(item: GroceryItemEntity) = withContext(Dispatchers.IO) {
        groceryDao.updateItem(item)
    }

    suspend fun clearAllSelected() = withContext(Dispatchers.IO) {
        groceryDao.clearAllSelected()
    }

    suspend fun selectAllInCategory(category: String, isSelected: Boolean) = withContext(Dispatchers.IO) {
        groceryDao.setCategorySelection(category, isSelected)
    }

    suspend fun deleteItem(item: GroceryItemEntity) = withContext(Dispatchers.IO) {
        groceryDao.deleteItem(item)
    }

    suspend fun checkInitialSeed(initialItemsSupplier: suspend () -> Unit) = withContext(Dispatchers.IO) {
        if (groceryDao.getItemCount() == 0) {
            initialItemsSupplier()
        }
    }
}
