package com.example.chessmate.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.viewmodel.OnlineChessViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun LoadingScreen(
    navController: NavController,
    viewModel: OnlineChessViewModel = viewModel()
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.matchId.value) {
        if (viewModel.matchId.value != null) {
            navController.navigate("play_with_opponent/${viewModel.matchId.value}") {
                popUpTo("loading") { inclusive = true }
            }
        }
    }

    LaunchedEffect(viewModel.matchmakingError.value) {
        viewModel.matchmakingError.value?.let { message ->
            errorMessage = message
            showErrorDialog = true
        }
    }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.set("cancelMatchmaking", false)
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("cancelMatchmaking")?.observeForever { shouldCancel ->
            if (shouldCancel) {
                viewModel.cancelMatchmaking()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Logo()
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Đang tìm đối thủ phù hợp...",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                navController.popBackStack()
            },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Lỗi",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "Đã có lỗi xảy ra.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}