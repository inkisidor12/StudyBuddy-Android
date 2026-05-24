package com.example.hito4.data.dao

import androidx.room.*
import com.example.hito4.data.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE uid = :uid ORDER BY timestamp ASC")
    fun getAllMessages(uid: String): Flow<List<ChatMessage>>

    @Insert
    suspend fun insert(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE uid = :uid")
    suspend fun clearAll(uid: String)
}