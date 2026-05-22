package com.taybeti.app.data.entities

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
    val modifiedDate: Long,
    val attachments: String = "[]"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NoteEntity
        return id == other.id && title == other.title && salt.contentEquals(other.salt) &&
            iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext) &&
            tag.contentEquals(other.tag) && isEncrypted == other.isEncrypted &&
            isFavorite == other.isFavorite && isDeleted == other.isDeleted &&
            isDecoyNote == other.isDecoyNote && createdDate == other.createdDate &&
            modifiedDate == other.modifiedDate && attachments == other.attachments
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        result = 31 * result + tag.contentHashCode()
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + isDecoyNote.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + modifiedDate.hashCode()
        result = 31 * result + attachments.hashCode()
        return result
    }
}
