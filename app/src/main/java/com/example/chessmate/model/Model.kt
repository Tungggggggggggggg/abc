package com.example.chessmate.model


data class FriendRequest(
    val fromUserId: String,
    val fromName: String,
    val toUserId: String
)

data class User(
    val userId: String,
    val name: String,
    val email: String
)
