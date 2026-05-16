package com.nulldata.app.di

import android.content.Context
import com.nulldata.app.data.database.AppDatabase
import com.nulldata.app.data.repository.NoteRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val noteRepository = NoteRepository(database.loginDao(), database.noteDao())
}
