package com.example.chessmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType

@Composable
fun PromotionDialog(
    playerColor: PieceColor,
    onSelect: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
        title = {
            Text(
                text = "Chọn quân để phong cấp",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PromotionButton(
                        pieceType = PieceType.QUEEN,
                        playerColor = playerColor,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                    PromotionButton(
                        pieceType = PieceType.BISHOP,
                        playerColor = playerColor,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PromotionButton(
                        pieceType = PieceType.KNIGHT,
                        playerColor = playerColor,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                    PromotionButton(
                        pieceType = PieceType.ROOK,
                        playerColor = playerColor,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = colorResource(id = R.color.color_c97c5d)
    )
}

@Composable
fun PromotionButton(
    pieceType: PieceType,
    playerColor: PieceColor,
    onSelect: (PieceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = when (pieceType) {
        PieceType.QUEEN -> if (playerColor == PieceColor.WHITE) R.drawable.white_queen else R.drawable.black_queen
        PieceType.BISHOP -> if (playerColor == PieceColor.WHITE) R.drawable.white_bishop else R.drawable.black_bishop
        PieceType.KNIGHT -> if (playerColor == PieceColor.WHITE) R.drawable.white_knight else R.drawable.black_knight
        PieceType.ROOK -> if (playerColor == PieceColor.WHITE) R.drawable.white_rook else R.drawable.black_rook
        else -> R.drawable.profile
    }
    val text = when (pieceType) {
        PieceType.QUEEN -> "Hậu"
        PieceType.ROOK -> "Xe"
        PieceType.BISHOP -> "Tượng"
        PieceType.KNIGHT -> "Mã"
        else -> ""
    }

    Button(
        onClick = { onSelect(pieceType) },
        modifier = modifier
            .height(60.dp)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.color_c89f9c)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 12.sp
            )
        }
    }
}