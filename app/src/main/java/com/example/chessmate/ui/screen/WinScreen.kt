package com.example.chessmate.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.chessmate.R
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WinScreen(
    onPlayAgain: () -> Unit,
    onBackToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { /* Ngăn tương tác với màn hình chính */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(colorResource(id = R.color.color_eed7c5).copy(alpha = 0.7f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Biểu tượng cúp vàng
            Image(
                painter = painterResource(id = R.drawable.win),
                contentDescription = "Cúp chiến thắng",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Văn bản "CHIẾN THẮNG!"
            Text(
                text = "CHIẾN THẮNG!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thông tin chi tiết về chiến thắng
            Text(
                text = "Bạn đã chiếu hết đối thủ!",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút "Chơi lại"
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c97c5d)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Chơi lại",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nút "Về màn hình chính"
            OutlinedButton(
                onClick = onBackToMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Về màn hình chính",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWinScreen() {
    WinScreen(
        onPlayAgain = {},
        onBackToMain = {}
    )
}