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
import com.example.chessmate.model.ChessPiece
import com.example.chessmate.model.Move
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType
import com.example.chessmate.model.Position

@Composable
fun Chessboard(
    board: Array<Array<ChessPiece?>>,
    highlightedSquares: List<Move>,
    onSquareClicked: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .wrapContentSize(Alignment.Center)
            .border(5.dp, colorResource(id = R.color.color_c89f9c))
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier
                .size(width = 360.dp, height = 20.dp)
                .background(colorResource(id = R.color.color_c89f9c))
        ) {}
        Row {
            Column(
                modifier = Modifier
                    .size(width = 20.dp, height = 320.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            ) {}
            Column {
                for (row in 7 downTo 0) {
                    Row {
                        for (col in 0 until 8) {
                            val isWhiteSquare = (row + col) % 2 == 1
                            val squareColor = if (isWhiteSquare) Color.White else colorResource(id = R.color.color_b36a5e)
                            val position = Position(row, col)
                            val highlight = highlightedSquares.find { it.position == position }
                            val isHighlighted = highlight != null
                            val isCaptureMove = highlight?.captures == true

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(squareColor)
                                    .clickable { onSquareClicked(row, col) },
                                contentAlignment = Alignment.Center
                            ) {
                                val piece = board[row][col]
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
            Column {
                for (row in 7 downTo 0) {
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 40.dp)
                            .background(colorResource(id = R.color.color_c89f9c)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${row + 1}",
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Row {
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 20.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            )
            for (col in 0 until 8) {
                val letter = ('A' + col).toString()
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .background(colorResource(id = R.color.color_c89f9c)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 20.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            )
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