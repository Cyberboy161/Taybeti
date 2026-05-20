package com.taybeti.app.di

import android.content.Context
import com.taybeti.app.data.database.AppDatabase
import com.taybeti.app.data.repository.NoteRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val noteRepository = NoteRepository(database.loginDao(), database.noteDao())
}
