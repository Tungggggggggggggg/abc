package com.example.chessmate.model

data class ChatMessage(
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val sequence: Long = 0,
    val readBy: List<String> = emptyList()
)