package com.nulldata.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_info")
data class LoginInfo(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val loginSalt: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val loginIv: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val loginCiphertext: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val loginTag: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val decoySalt: ByteArray? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val decoyIv: ByteArray? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val decoyCiphertext: ByteArray? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val decoyTag: ByteArray? = null,
    val decoyEnabled: Boolean = false,
    val failedAttempts: Int = 0,
    val lastFailedTime: Long = 0L
)
