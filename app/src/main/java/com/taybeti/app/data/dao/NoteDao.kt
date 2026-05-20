package com.taybeti.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taybeti.app.data.entities.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isDecoyNote = :isDecoy ORDER BY modifiedDate DESC")
    suspend fun getAllActive(isDecoy: Boolean = false): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 AND isDecoyNote = :isDecoy ORDER BY modifiedDate DESC")
    suspend fun getTrash(isDecoy: Boolean = false): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isDeleted = 0 AND isDecoyNote = :isDecoy ORDER BY modifiedDate DESC")
    suspend fun getFavorites(isDecoy: Boolean = false): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getById(noteId: String): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM notes WHERE isDecoyNote = :isDecoy")
    suspend fun getAllByDecoy(isDecoy: Boolean): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
