package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.chessmate.viewmodel.ChatViewModel
import com.example.chessmate.viewmodel.FriendWithLastMessage
import java.net.URLEncoder
import android.util.Log

@Composable
fun ChatScreen(
    navController: NavController? = null,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val friendsWithMessages by viewModel.friendsWithMessages.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFriendsWithMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        ChatHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (friendsWithMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có bạn bè nào.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            } else {
                friendsWithMessages.forEachIndexed { index, friendWithMessage ->
                    FriendListItem(
                        friendWithMessage = friendWithMessage,
                        onClick = {
                            Log.d("ChatScreen", "Navigating to friend: ID=${friendWithMessage.friend.userId}, Name=${friendWithMessage.friend.name}")
                            val encodedFriendName = URLEncoder.encode(friendWithMessage.friend.name, "UTF-8")
                            navController?.navigate("chat_detail/${friendWithMessage.friend.userId}/$encodedFriendName")
                        },
                        navController = navController
                    )
                    if (index < friendsWithMessages.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
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
            text = "Bạn bè",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.width(44.dp))
    }
}

@Composable
fun FriendListItem(
    friendWithMessage: FriendWithLastMessage,
    onClick: () -> Unit,
    navController: NavController?
) {
    val friend = friendWithMessage.friend
    val lastMessage = friendWithMessage.lastMessage
    val hasUnread = friendWithMessage.hasUnread
    val currentUserId = friendWithMessage.friend.userId // Sửa lỗi: không tạo instance mới

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    navController?.navigate("competitor_profile/${friend.userId}")
                }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Text(
                text = friend.name,
                fontSize = 18.sp,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    lastMessage == null -> "Chưa có tin nhắn"
                    lastMessage.senderId == currentUserId -> "Bạn: ${lastMessage.message}"
                    else -> lastMessage.message
                },
                fontSize = 14.sp,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                color = Color.Black,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.Top)
            )
        }
    }
}