package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme

// Thanh tiêu đề với nút quay lại và tiêu đề "Quên mật khẩu"
@Composable
fun ForgotPasswordHeader(
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
            text = "Quên mật khẩu",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(44.dp))
    }
}

// Form nhập email hoặc ID để khôi phục mật khẩu
@Composable
fun ForgotPasswordForm(
    modifier: Modifier = Modifier,
    onSubmitClick: (String) -> Unit // Truyền email hoặc ID khi gửi
) {
    var emailOrId by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ô nhập email hoặc ID
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Email hoặc ID:",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            BasicTextField(
                value = emailOrId,
                onValueChange = { emailOrId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colorResource(id = R.color.color_eee2df), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    if (emailOrId.isEmpty()) {
                        Text(
                            text = "Nhập email hoặc ID ...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Nút gửi mã khôi phục
        Button(
            onClick = {
                keyboardController?.hide()
                onSubmitClick(emailOrId)
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Gửi mã khôi phục",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Màn hình chính để khôi phục mật khẩu
@Composable
fun ForgotPasswordScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
    ) {
        ForgotPasswordHeader(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colorResource(id = R.color.color_c97c5d)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo ứng dụng
            Logo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Spacer(modifier = Modifier.height(20.dp))
            ForgotPasswordForm(
                onSubmitClick = { emailOrId ->
                    // Giả lập gửi mã khôi phục
                    // TODO: Tích hợp với backend để gửi email chứa mã khôi phục
                    navController?.navigate("reset_password")
                }
            )
        }
    }
}

// Xem trước giao diện màn hình khôi phục mật khẩu
@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ChessmateTheme {
        ForgotPasswordScreen()
    }
}