package com.example.chessmate.model

data class Match(
    val result: String,
    val date: String,
    val moves: Int,
    val opponent: String
)