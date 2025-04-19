package com.example.chessmate.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.chessmate.model.ChatMessage
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PromotionDialog
import com.example.chessmate.viewmodel.FindFriendsViewModel
import com.example.chessmate.viewmodel.OnlineChessViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PlayWithOpponentHeader(
    onBackClick: () -> Unit,
    onExitConfirm: () -> Unit,
    opponentName: String,
    opponentScore: Int,
    whiteTime: Int,
    blackTime: Int,
    playerColor: PieceColor?,
    onFriendRequestSent: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var isFriendRequestSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                contentDescription = "$opponentName",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onProfileClick() },
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
                    text = formatTime(if (playerColor == PieceColor.BLACK) whiteTime else blackTime),
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
                .background(
                    colorResource(id = if (isFriendRequestSent) R.color.color_b36a5e else R.color.color_eed7c5),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(enabled = !isFriendRequestSent) {
                    if (!isFriendRequestSent) {
                        onFriendRequestSent()
                        isFriendRequestSent = true
                        Toast.makeText(context, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFriendRequestSent) "Đã gửi" else "+ Kết bạn",
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
    blackTime: Int,
    playerColor: PieceColor?,
    hasUnreadMessages: Boolean,
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
                contentDescription = "$playerName",
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
            if (hasUnreadMessages) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .background(Color.Red, shape = CircleShape)
                )
            }
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
                text = formatTime(if (playerColor == PieceColor.WHITE) whiteTime else blackTime),
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
fun ChatDialog(
    messages: List<ChatMessage>,
    currentUserId: String?,
    playerName: String,
    opponentName: String,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Tự động cuộn đến tin nhắn mới nhất khi danh sách thay đổi
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(colorResource(id = R.color.color_c97c5d))
            .height(400.dp),
        title = {
            Text(
                text = "Chat",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(messages) { _, message ->
                        val isCurrentUser = message.senderId == currentUserId
                        val senderName = if (isCurrentUser) playerName else opponentName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        colorResource(
                                            id = if (isCurrentUser) R.color.color_eed7c5 else R.color.color_c89f9c
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "$senderName: ${message.message}",
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        placeholder = { Text("Nhập tin nhắn...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendMessage(messageInput)
                                messageInput = ""
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Gửi",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color.White)
            }
        },
        containerColor = colorResource(id = R.color.color_c97c5d)
    )
}

@Composable
fun PlayWithOpponentScreen(
    navController: NavController? = null,
    matchId: String = "",
    onBackClick: () -> Unit = { navController?.popBackStack() },
    viewModel: OnlineChessViewModel = viewModel(),
    friendViewModel: FindFriendsViewModel = viewModel()
) {
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showDrawRequestDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("Bạn") }
    var opponentName by remember { mutableStateOf("Đối thủ") }
    var playerScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }
    var opponentId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val sentRequests by friendViewModel.sentRequests.collectAsState()
    val friends by friendViewModel.friends.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Đánh dấu tin nhắn đã đọc khi mở dialog
    LaunchedEffect(showChatDialog) {
        if (showChatDialog) {
            viewModel.markMessagesAsRead()
        }
    }

    LaunchedEffect(matchId) {
        if (viewModel.matchId.value != matchId) {
            viewModel.matchId.value = matchId
            viewModel.listenToMatchUpdates()
        }
    }

    LaunchedEffect(viewModel.matchId.value) {
        viewModel.matchId.value?.let { id ->
            val db = Firebase.firestore
            db.collection("matches").document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val player1Id = snapshot.getString("player1")
                    val player2Id = snapshot.getString("player2")
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                    if (player1Id != null && player2Id != null && currentUserId != null) {
                        val expectedColor = if (currentUserId == player1Id) PieceColor.WHITE else PieceColor.BLACK
                        if (viewModel.playerColor.value != expectedColor) {
                            viewModel.playerColor.value = expectedColor
                        }

                        opponentId = if (currentUserId == player1Id) player2Id else player1Id

                        db.collection("users").document(player1Id)
                            .addSnapshotListener { player1Doc, player1Error ->
                                if (player1Error != null || player1Doc == null) return@addSnapshotListener
                                val player1Name = player1Doc.getString("name") ?: player1Doc.getString("username") ?: "Người chơi 1"
                                val player1Score = player1Doc.getLong("score")?.toInt() ?: 0

                                db.collection("users").document(player2Id)
                                    .addSnapshotListener { player2Doc, player2Error ->
                                        if (player2Error != null || player2Doc == null) return@addSnapshotListener
                                        val player2Name = player2Doc.getString("name") ?: player2Doc.getString("username") ?: "Người chơi 2"
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
                blackTime = viewModel.blackTime.value,
                playerColor = viewModel.playerColor.value,
                onFriendRequestSent = {
                    opponentId?.let { id ->
                        if (id !in sentRequests && friends.none { it.userId == id }) {
                            friendViewModel.sendFriendRequest(id)
                        } else {
                            Toast.makeText(context, "Lời mời đã được gửi hoặc đã là bạn bè!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onProfileClick = {
                    opponentId?.let { id ->
                        navController?.navigate("competitor_profile/$id")
                    }
                }
            )
            Text(
                text = "Bạn là bên ${if (viewModel.playerColor.value == PieceColor.WHITE) "trắng" else "đen"}",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            Chessboard(
                board = viewModel.board.value,
                highlightedSquares = viewModel.highlightedSquares.value,
                onSquareClicked = { row, col -> viewModel.onSquareClicked(row, col) },
                playerColor = viewModel.playerColor.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Text(
                text = viewModel.moveHistory.lastOrNull() ?: "Chưa có nước đi",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            PlayWithOpponentFooter(
                onOfferDraw = { viewModel.requestDraw() },
                onSurrender = {
                    viewModel.surrender()
                    showGameOverDialog = true
                },
                playerName = playerName,
                playerScore = playerScore,
                whiteTime = viewModel.whiteTime.value,
                blackTime = viewModel.blackTime.value,
                playerColor = viewModel.playerColor.value,
                hasUnreadMessages = viewModel.hasUnreadMessages.value,
                onChatClick = { showChatDialog = true }
            )
        }
    }

    if (viewModel.isPromoting.value) {
        PromotionDialog(
            playerColor = viewModel.playerColor.value ?: PieceColor.WHITE,
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
                        if (navController != null) {
                            navController.popBackStack()
                        } else {
                            onBackClick()
                        }
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
            dismissButton = {},
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }

    if (showChatDialog) {
        ChatDialog(
            messages = viewModel.chatMessages,
            currentUserId = currentUserId,
            playerName = playerName,
            opponentName = opponentName,
            onSendMessage = { message -> viewModel.sendMessage(message) },
            onDismiss = { showChatDialog = false }
        )
    }
}