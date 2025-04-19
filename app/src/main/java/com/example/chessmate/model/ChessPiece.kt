package com.example.chessmate.model

enum class PieceColor {
    WHITE, BLACK
}

enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val position: Position
)

data class Move(
    val from: Position,
    val position: Position,
    val captures: Boolean
)