package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.GroceryRepository
import com.example.voice.VoiceSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GharKiListApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GroceryRepository(database.groceryDao()) }
    val voiceSpeechManager by lazy { VoiceSpeechManager(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
