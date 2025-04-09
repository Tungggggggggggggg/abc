package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme

// Thanh tiêu đề với nút quay lại và tiêu đề "Đặt lại mật khẩu"
@Composable
fun ResetPasswordHeader(
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
            text = "Đặt lại mật khẩu",
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

// Form nhập mã khôi phục và mật khẩu mới
@Composable
fun ResetPasswordForm(
    modifier: Modifier = Modifier,
    onSubmitClick: (String, String) -> Unit // Truyền mã khôi phục và mật khẩu mới
) {
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ô nhập mã khôi phục
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mã khôi phục:",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            BasicTextField(
                value = code,
                onValueChange = { code = it },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    if (code.isEmpty()) {
                        Text(
                            text = "Nhập mã khôi phục ...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Ô nhập mật khẩu mới
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mật khẩu mới:",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            BasicTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                decorationBox = { innerTextField ->
                    if (newPassword.isEmpty()) {
                        Text(
                            text = "Nhập mật khẩu mới ...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Nút xác nhận
        Button(
            onClick = {
                keyboardController?.hide()
                onSubmitClick(code, newPassword)
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Xác nhận",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Màn hình chính để đặt lại mật khẩu
@Composable
fun ResetPasswordScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
    ) {
        ResetPasswordHeader(onBackClick = onBackClick)
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
            ResetPasswordForm(
                onSubmitClick = { code, newPassword ->
                    // Giả lập đặt lại mật khẩu
                    // TODO: Tích hợp với backend để xác nhận mã và cập nhật mật khẩu mới
                    navController?.navigate("login") // Quay lại màn hình đăng nhập sau khi đặt lại mật khẩu
                }
            )
        }
    }
}

// Xem trước giao diện màn hình đặt lại mật khẩu
@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    ChessmateTheme {
        ResetPasswordScreen()
    }
}