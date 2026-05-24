package com.example.hito4.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val uid: String = ""
)