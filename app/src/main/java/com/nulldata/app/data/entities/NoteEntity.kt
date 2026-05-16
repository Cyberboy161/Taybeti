package com.nulldata.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val salt: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val iv: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val ciphertext: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val tag: ByteArray,
    val isEncrypted: Boolean = true,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val isDecoyNote: Boolean = false,
    val createdDate: Long,
    val modifiedDate: Long
)
