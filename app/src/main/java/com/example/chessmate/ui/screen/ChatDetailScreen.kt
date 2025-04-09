package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.theme.ChessmateTheme

// Thanh tiêu đề (App Bar)
@Composable
fun ChatDetailHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Người dùng",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}

// Nội dung chính của màn hình Chat Detail
@Composable
fun ChatDetailContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        // Phần "Tin nhắn chưa đọc" với đường gạch ngang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                color = Color.White,
                thickness = 1.dp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "Tin nhắn chưa đọc",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
            HorizontalDivider(
                color = Color.White,
                thickness = 1.dp,
                modifier = Modifier.weight(1f)
            )
        }

        // Nội dung trò chuyện sẽ được thêm ở đây
        // Ví dụ: danh sách tin nhắn, hình ảnh, v.v.
    }
}

// Vùng nhập tin nhắn
@Composable
fun ChatInput(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.image),
                contentDescription = "Hình ảnh",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.mic),
                contentDescription = "Micro",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = "", // Giá trị nhập tin nhắn
                onValueChange = { /* Xử lý thay đổi giá trị nhập */ },
                placeholder = { Text("Nhập tin nhắn...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Xử lý gửi tin nhắn */ }) {
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Gửi",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Màn hình chính để hiển thị Chat Detail
@Composable
fun ChatDetailScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChatDetailHeader(onBackClick = onBackClick)
        ChatDetailContent(
            modifier = Modifier
                .weight(1f)
        )
        ChatInput()
    }
}

// Xem trước giao diện màn hình Chat Detail
@Preview(showBackground = true)
@Composable
fun ChatDetailScreenPreview() {
    ChessmateTheme {
        ChatDetailScreen()
    }
}