package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Dữ liệu người dùng
data class User(val userId: String, val name: String, val email: String = "")

// ViewModel quản lý danh sách bạn bè
class ChatViewModel : ViewModel() {
    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> get() = _friends

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadFriends()
    }

    fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friends")
            .whereIn("user1", listOf(currentUserId))
            .get()
            .addOnSuccessListener { result1 ->
                firestore.collection("friends")
                    .whereIn("user2", listOf(currentUserId))
                    .get()
                    .addOnSuccessListener { result2 ->
                        val allDocs = result1.documents + result2.documents
                        val fetchedFriends = mutableSetOf<User>()
                        allDocs.forEach { doc ->
                            val user1 = doc.getString("user1")
                            val user2 = doc.getString("user2")
                            val friendId = if (user1 == currentUserId) user2 else user1
                            if (!friendId.isNullOrBlank()) {
                                firestore.collection("users").document(friendId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val name = userDoc.getString("name") ?: "Unknown"
                                        val email = userDoc.getString("email") ?: ""
                                        fetchedFriends.add(User(friendId, name, email))
                                        _friends.value = fetchedFriends.toList()
                                    }
                            }
                        }
                    }
            }
    }

    fun removeFriend(friend: User) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Xóa mối quan hệ từ cả hai hướng: user1 và user2
        firestore.collection("friends")
            .whereEqualTo("user1", currentUserId)
            .whereEqualTo("user2", friend.userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.firstOrNull()?.reference?.delete()?.addOnSuccessListener {
                    // Cập nhật lại danh sách bạn bè sau khi xóa
                    _friends.value = _friends.value.filter { it.userId != friend.userId }
                }
            }

        firestore.collection("friends")
            .whereEqualTo("user1", friend.userId)
            .whereEqualTo("user2", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.firstOrNull()?.reference?.delete()?.addOnSuccessListener {
                    // Cập nhật lại danh sách bạn bè sau khi xóa
                    _friends.value = _friends.value.filter { it.userId != friend.userId }
                }
            }
    }
}
    // Giao diện màn hình chat
    @Composable
    fun ChatScreen(
        navController: NavController? = null,
        viewModel: ChatViewModel = viewModel(),
        onBackClick: () -> Unit = { navController?.popBackStack() }
    ) {
        val friends by viewModel.friends.collectAsState()
        var showConfirmationDialog by remember { mutableStateOf<Pair<User, Boolean>?>(null) }

        LaunchedEffect(Unit) {
            viewModel.loadFriends()
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
                if (friends.isEmpty()) {
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
                    friends.forEach { friend ->
                        FriendListItem(
                            friend = friend,
                            onRemoveFriend = { showConfirmationDialog = Pair(friend, true) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Dialog xác nhận xóa bạn bè
            showConfirmationDialog?.let { (friend, show) ->
                if (show) {
                    AlertDialog(
                        onDismissRequest = { showConfirmationDialog = null },
                        title = {
                            Text(
                                text = "Xóa bạn bè",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text("Bạn có chắc chắn muốn xóa '${friend.name}' khỏi danh sách bạn bè không?")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.removeFriend(friend)
                                showConfirmationDialog = null
                            }) {
                                Text("Xóa", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmationDialog = null }) {
                                Text("Hủy")
                            }
                        }
                    )
                }
            }
        }
    }


// Phần header có nút quay lại
@Composable
fun ChatHeader(onBackClick: () -> Unit) {
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

        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = " Bạn bè",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}

// Giao diện mỗi bạn bè
@Composable
fun FriendListItem(friend: User, onRemoveFriend: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black) // Viền đen xung quanh
            .background(colorResource(id = R.color.color_c97c5d))
            .padding(12.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = friend.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onRemoveFriend(friend) }) {
            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Xóa bạn bè",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
