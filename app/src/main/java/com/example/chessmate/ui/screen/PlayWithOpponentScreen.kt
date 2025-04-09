package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PromotionDialog
import com.example.chessmate.viewmodel.OnlineChessViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@Composable
fun PlayWithOpponentHeader(
    onBackClick: () -> Unit,
    onExitConfirm: () -> Unit,
    opponentName: String,
    opponentScore: Int,
    whiteTime: Int,
    blackTime: Int,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = CircleShape)
                .clickable { showExitDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "X",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Đối thủ",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = opponentName,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-106).dp, y = 10.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Điểm: $opponentScore",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .offset(x = 8.dp)
                    .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(blackTime),
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 106.dp)
                .padding(top = 24.dp)
                .width(94.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { /* TODO: Xử lý kết bạn */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Kết bạn",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Thoát trận đấu",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn thoát trận đấu không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onExitConfirm()
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
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}

@Composable
fun PlayWithOpponentFooter(
    onOfferDraw: () -> Unit,
    onSurrender: () -> Unit,
    playerName: String,
    playerScore: Int,
    whiteTime: Int,
    onChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDrawDialog by remember { mutableStateOf(false) }
    var showSurrenderDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(108.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Bạn",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = playerName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Điểm: $playerScore",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-14).dp, y = 10.dp)
                .padding(top = 20.dp)
                .width(80.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { showDrawDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cầu hòa",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 86.dp, y = 10.dp)
                .padding(top = 20.dp)
                .width(90.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { showSurrenderDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đầu hàng",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-30).dp, y = (-36).dp)
                .padding(top = 20.dp)
                .width(38.dp)
                .height(38.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { onChatClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.message),
                contentDescription = "Message",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 24.dp)
                .offset(x = (-56).dp, y = (-36).dp)
                .width(82.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(whiteTime),
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDrawDialog) {
        AlertDialog(
            onDismissRequest = { showDrawDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Cầu hòa",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn gửi yêu cầu cầu hòa không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDrawDialog = false
                        onOfferDraw()
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
            dismissButton = {
                TextButton(onClick = { showDrawDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    if (showSurrenderDialog) {
        AlertDialog(
            onDismissRequest = { showSurrenderDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Đầu hàng",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đầu hàng không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSurrenderDialog = false
                        onSurrender()
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
            dismissButton = {
                TextButton(onClick = { showSurrenderDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}

@Composable
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun PlayWithOpponentScreen(
    navController: NavController? = null,
    matchId: String = "",
    onBackClick: () -> Unit = { navController?.popBackStack() },
    viewModel: OnlineChessViewModel = viewModel()
) {
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showDrawRequestDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("Bạn") }
    var opponentName by remember { mutableStateOf("Đối thủ") }
    var playerScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }

    LaunchedEffect(matchId) {
        if (viewModel.matchId.value != matchId) {
            viewModel.matchId.value = matchId
            viewModel.listenToMatchUpdates()
        }
    }

    // Lấy thông tin người chơi từ Firestore
    LaunchedEffect(viewModel.matchId.value) {
        viewModel.matchId.value?.let { id ->
            val db = Firebase.firestore
            val matchDoc = db.collection("matches").document(id).get().await()
            val player1Id = matchDoc.getString("player1")
            val player2Id = matchDoc.getString("player2")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (player1Id != null && player2Id != null) {
                val player1Doc = db.collection("users").document(player1Id).get().await()
                val player2Doc = db.collection("users").document(player2Id).get().await()

                val player1Name = player1Doc.getString("displayName") ?: "Người chơi 1"
                val player2Name = player2Doc.getString("displayName") ?: "Người chơi 2"
                val player1Score = player1Doc.getLong("score")?.toInt() ?: 0
                val player2Score = player2Doc.getLong("score")?.toInt() ?: 0

                if (currentUserId == player1Id) {
                    playerName = player1Name
                    playerScore = player1Score
                    opponentName = player2Name
                    opponentScore = player2Score
                } else {
                    playerName = player2Name
                    playerScore = player2Score
                    opponentName = player1Name
                    opponentScore = player1Score
                }
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
            PlayWithOpponentHeader(
                onBackClick = onBackClick,
                onExitConfirm = {
                    viewModel.surrender()
                    onBackClick()
                },
                opponentName = opponentName,
                opponentScore = opponentScore,
                whiteTime = viewModel.whiteTime.value,
                blackTime = viewModel.blackTime.value
            )
            Chessboard(
                board = viewModel.board.value,
                highlightedSquares = viewModel.highlightedSquares.value,
                onSquareClicked = { row, col -> viewModel.onSquareClicked(row, col) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            PlayWithOpponentFooter(
                onOfferDraw = { viewModel.requestDraw() },
                onSurrender = {
                    viewModel.surrender()
                    showGameOverDialog = true
                },
                playerName = playerName,
                playerScore = playerScore,
                whiteTime = viewModel.whiteTime.value
            )
        }
    }

    if (viewModel.isPromoting.value) {
        PromotionDialog(
            currentTurn = viewModel.currentTurn.value,
            onSelect = { pieceType ->
                viewModel.promotePawn(pieceType)
            },
            onDismiss = {}
        )
    }

    if (viewModel.drawRequest.value != null && viewModel.drawRequest.value != FirebaseAuth.getInstance().currentUser?.uid) {
        showDrawRequestDialog = true
    }

    if (showDrawRequestDialog) {
        AlertDialog(
            onDismissRequest = { showDrawRequestDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Yêu cầu cầu hòa",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Đối thủ đã gửi yêu cầu cầu hòa. Bạn có đồng ý không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDrawRequestDialog = false
                        viewModel.acceptDraw()
                        showGameOverDialog = true
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
            dismissButton = {
                TextButton(onClick = {
                    showDrawRequestDialog = false
                    viewModel.declineDraw()
                }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    if (viewModel.isGameOver.value || showGameOverDialog) {
        AlertDialog(
            onDismissRequest = {},
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Ván đấu kết thúc",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = viewModel.gameResult.value ?: "Trò chơi kết thúc.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGameOverDialog = false
                        navController?.popBackStack()
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