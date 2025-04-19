package com.example.chessmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.R
import com.example.chessmate.model.*

@Composable
fun Chessboard(
    board: Array<Array<ChessPiece?>>,
    highlightedSquares: List<Move>,
    onSquareClicked: (row: Int, col: Int) -> Unit,
    playerColor: PieceColor?,
    modifier: Modifier = Modifier
) {
    val isWhitePerspective = playerColor == PieceColor.WHITE || playerColor == null

    Column(
        modifier = modifier
            .wrapContentSize(Alignment.Center)
            .border(5.dp, colorResource(id = R.color.color_c89f9c))
            .padding(5.dp)
    ) {
        // Nhãn cột (A-H hoặc H-A)
        Row(
            modifier = Modifier
                .size(width = 360.dp, height = 20.dp)
                .background(colorResource(id = R.color.color_c89f9c))
        ) {
            Box(modifier = Modifier.size(width = 20.dp, height = 20.dp))
            val cols = if (isWhitePerspective) ('A'..'H') else ('H' downTo 'A')
            cols.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(modifier = Modifier.size(width = 20.dp, height = 20.dp))
        }

        // Bàn cờ
        Row {
            // Nhãn hàng bên trái
            Column(
                modifier = Modifier
                    .size(width = 20.dp, height = 320.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            ) {
                val rows = if (isWhitePerspective) (8 downTo 1) else (1..8)
                rows.forEach { rowLabel ->
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowLabel.toString(),
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Các ô trên bàn cờ
            Column {
                for (row in 0 until 8) {
                    val displayRow = if (isWhitePerspective) 7 - row else row
                    Row {
                        for (col in 0 until 8) {
                            val displayCol = if (isWhitePerspective) col else 7 - col
                            val isWhiteSquare = (displayRow + displayCol) % 2 == 1
                            val squareColor = if (isWhiteSquare) Color.White else colorResource(id = R.color.color_b36a5e)
                            val position = Position(displayRow, displayCol)
                            val highlight = highlightedSquares.find { it.position == position }
                            val isHighlighted = highlight != null
                            val isCaptureMove = highlight?.captures == true

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(squareColor)
                                    .clickable { onSquareClicked(displayRow, displayCol) },
                                contentAlignment = Alignment.Center
                            ) {
                                val piece = board[displayRow][displayCol]
                                if (piece != null) {
                                    val pieceDrawable = getPieceDrawable(piece)
                                    Image(
                                        painter = painterResource(id = pieceDrawable),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                if (isHighlighted) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = if (isCaptureMove) Color.Red else Color(0xFF90EE90),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Nhãn hàng bên phải
            Column(
                modifier = Modifier
                    .size(width = 20.dp, height = 320.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            ) {
                val rows = if (isWhitePerspective) (8 downTo 1) else (1..8)
                rows.forEach { rowLabel ->
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowLabel.toString(),
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Nhãn cột dưới cùng
        Row(
            modifier = Modifier
                .size(width = 360.dp, height = 20.dp)
                .background(colorResource(id = R.color.color_c89f9c))
        ) {
            Box(modifier = Modifier.size(width = 20.dp, height = 20.dp))
            val cols = if (isWhitePerspective) ('A'..'H') else ('H' downTo 'A')
            cols.forEach { letter ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(modifier = Modifier.size(width = 20.dp, height = 20.dp))
        }
    }
}

@Composable
fun getPieceDrawable(piece: ChessPiece): Int {
    return when (piece.type) {
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) R.drawable.white_pawn else R.drawable.black_pawn
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) R.drawable.white_rook else R.drawable.black_rook
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) R.drawable.white_knight else R.drawable.black_knight
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) R.drawable.white_bishop else R.drawable.black_bishop
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) R.drawable.white_queen else R.drawable.black_queen
        PieceType.KING -> if (piece.color == PieceColor.WHITE) R.drawable.white_king else R.drawable.black_king
    }
}