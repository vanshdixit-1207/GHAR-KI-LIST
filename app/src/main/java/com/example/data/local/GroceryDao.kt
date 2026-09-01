package com.example.data.local

import androidx.room.*
import com.example.data.model.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_items ORDER BY category ASC, orderIndex ASC, id ASC")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE isSelected = 1 ORDER BY category ASC, orderIndex ASC")
    fun getSelectedItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY orderIndex ASC, id ASC")
    fun getItemsByCategory(category: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): GroceryItemEntity?

    @Query("SELECT COUNT(*) FROM grocery_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GroceryItemEntity>)

    @Update
    suspend fun updateItem(item: GroceryItemEntity)

    @Query("UPDATE grocery_items SET isSelected = :isSelected WHERE id = :id")
    suspend fun toggleSelection(id: Int, isSelected: Boolean)

    @Query("UPDATE grocery_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: String)

    @Query("UPDATE grocery_items SET isSelected = 0")
    suspend fun clearAllSelected()

    @Query("UPDATE grocery_items SET isSelected = :isSelected WHERE category = :category")
    suspend fun setCategorySelection(category: String, isSelected: Boolean)

    @Delete
    suspend fun deleteItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)
}
