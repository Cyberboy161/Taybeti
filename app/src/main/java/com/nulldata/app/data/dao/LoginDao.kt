package com.nulldata.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nulldata.app.data.entities.LoginInfo

@Dao
interface LoginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: LoginInfo)

    @Query("SELECT * FROM login_info WHERE id = 1")
    suspend fun get(): LoginInfo?

    @Query("UPDATE login_info SET failedAttempts = :attempts, lastFailedTime = :time WHERE id = 1")
    suspend fun updateFailedAttempts(attempts: Int, time: Long)

    @Query("DELETE FROM login_info")
    suspend fun deleteAll()

    @Query("UPDATE login_info SET decoySalt = :salt, decoyIv = :iv, decoyCiphertext = :ct, decoyTag = :tag, decoyEnabled = 1 WHERE id = 1")
    suspend fun updateDecoy(salt: ByteArray, iv: ByteArray, ct: ByteArray, tag: ByteArray)

    @Query("UPDATE login_info SET loginSalt = :salt, loginIv = :iv, loginCiphertext = :ct, loginTag = :tag WHERE id = 1")
    suspend fun updateLogin(salt: ByteArray, iv: ByteArray, ct: ByteArray, tag: ByteArray)
}
