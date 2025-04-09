package com.example.chessmate.ui.screen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.chessmate.model.User
import com.example.chessmate.model.FriendRequest


// ViewModel for FindFriendsScreen
class FindFriendsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _receivedRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedRequests: StateFlow<List<FriendRequest>> = _receivedRequests

    private val _sentRequests = MutableStateFlow<List<String>>(emptyList())
    val sentRequests: StateFlow<List<String>> = _sentRequests

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends


    fun searchUsers(query: String) {
        firestore.collection("users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: return@mapNotNull null
                    val userId = doc.getString("userId") ?: doc.id
                    val currentUid = auth.currentUser?.uid
                    if (userId == currentUid) return@mapNotNull null
                    User(userId, name, email)
                }
                _searchResults.value = users
            }
    }


    fun sendFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid ?: return
        val fromUserName = auth.currentUser?.displayName ?: "Unknown" // Lấy tên người gửi

        // Lưu lời mời kết bạn với tên người gửi
        val request = hashMapOf(
            "fromUserId" to fromUserId,
            "fromName" to fromUserName,  // Lưu tên người gửi vào trường này
            "toUserId" to toUserId,
            "timestamp" to Timestamp.now(),
            "status" to "pending"
        )

        firestore.collection("friend_requests")
            .add(request)
            .addOnSuccessListener {
                // Cập nhật danh sách lời mời đã gửi
                _sentRequests.value = _sentRequests.value + toUserId
            }
    }


    fun acceptFriendRequest(request: FriendRequest) {
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", request.fromUserId)
            .whereEqualTo("toUserId", request.toUserId)
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()

                // Kiểm tra nếu requestDoc không null
                if (requestDoc != null) {
                    // Cập nhật trạng thái lời mời thành "accepted"
                    requestDoc.reference.update("status", "accepted")
                        .addOnSuccessListener {
                            // Thêm vào danh sách bạn bè
                            firestore.collection("friends")
                                .add(
                                    hashMapOf(
                                        "user1" to request.fromUserId,
                                        "user2" to request.toUserId
                                    )
                                )
                                .addOnSuccessListener {
                                    // Cập nhật danh sách bạn bè ngay lập tức
                                    _friends.value = _friends.value + User(
                                        request.fromUserId,
                                        request.fromName,
                                        ""
                                    )
                                    loadReceivedRequests() // Cập nhật lại các yêu cầu nhận được
                                    loadSentRequests() // Cập nhật lại các yêu cầu đã gửi
                                    loadFriends()
                                }
                            // Xóa lời mời kết bạn sau khi chấp nhận
                            requestDoc.reference.delete()
                                .addOnSuccessListener {
                                    // Cập nhật lại danh sách các yêu cầu đã nhận
                                    loadReceivedRequests()
                                }
                        }
                }
            }
    }


    fun declineFriendRequest(request: FriendRequest) {
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", request.fromUserId)
            .whereEqualTo("toUserId", request.toUserId)
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                requestDoc?.reference?.update("status", "declined")
                // Cập nhật lại các yêu cầu
                loadReceivedRequests()
                loadSentRequests()
            }
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



    fun loadReceivedRequests() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friend_requests")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val friendRequests = result.documents.mapNotNull { doc ->
                    val fromUserId = doc.getString("fromUserId") ?: return@mapNotNull null
                    val fromName =
                        doc.getString("fromName") ?: "Unknown" // Đảm bảo lấy tên người gửi
                    FriendRequest(fromUserId, fromName, currentUserId)
                }
                _receivedRequests.value = friendRequests
            }
    }


    fun loadSentRequests() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val sentRequestsList = result.documents.mapNotNull { doc ->
                    doc.getString("toUserId")
                }
                _sentRequests.value = sentRequestsList
            }
    }
}
@Composable
fun Header(
    navController: NavController,
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
        Box(
            modifier = Modifier
                .clickable { navController.navigate("chat") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.friend),
                contentDescription = "Tin nhắn",
                modifier = Modifier.size(32.dp)
            )

        }
        Text(
            text = "Tìm Bạn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        IconButton(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Hồ sơ",
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
fun BackButton(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp)
            .background(
                colorResource(id = R.color.color_c89f9c),
                shape = RoundedCornerShape(25.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "Nhập tên người chơi ...", // Placeholder text
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField() // This will display the text being entered by the user
            }
        )
        IconButton(onClick = onSearch) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Tìm kiếm",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun FindFriendsScreen(
    navController: NavController,
    viewModel: FindFriendsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val receivedRequests by viewModel.receivedRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val friends by viewModel.friends.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Load lại dữ liệu bạn bè khi người dùng đăng nhập lại
    LaunchedEffect(Unit) {
        viewModel.loadReceivedRequests()
        viewModel.loadSentRequests()
        viewModel.loadFriends()  // Đảm bảo tải lại danh sách bạn bè
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(navController = navController)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFC97C5D))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BackButton(onBackClick = { navController.popBackStack() })
            Logo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Spacer(modifier = Modifier.height(20.dp))

            SearchBar(
                text = searchQuery,
                onTextChanged = { searchQuery = it },
                onSearch = { viewModel.searchUsers(searchQuery) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Kết quả tìm kiếm
            searchResults.forEach { user ->
                val isFriend = friends.any { it.userId == user.userId }
                SearchResultItem(
                    user = user,
                    alreadySent = user.userId in sentRequests,
                    onAddFriend = { viewModel.sendFriendRequest(user.userId) },
                    isFriend = isFriend
                )
                Spacer(modifier = Modifier.height(8.dp))
            }



            // Hiển thị lời mời kết bạn đã nhận
            if (receivedRequests.isNotEmpty()) {
                Text(
                    text = "Lời mời kết bạn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                receivedRequests.forEach { request ->
                    FriendRequestItem(request = request,
                        onAccept = { viewModel.acceptFriendRequest(request) },
                        onDecline = { viewModel.declineFriendRequest(request) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

        }
    }
}
@Composable
fun SearchResultItem(
    user: User,
    alreadySent: Boolean,
    onAddFriend: () -> Unit,
    isFriend: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c)
            ,shape = RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = user.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        val backgroundColor = when {
            alreadySent -> Color.Red // Xám nâu
            isFriend -> Color((0xFF388E3C)) // Xám đỏ
            else -> Color.Blue // Mặc định (có thể đổi)
        }

        Button(
            onClick = onAddFriend,
            enabled = !alreadySent && !isFriend,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = Color.White,
                disabledContainerColor = backgroundColor, // Giữ màu khi disabled
                disabledContentColor = Color.White        // Giữ màu chữ khi disabled
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text(text = when {
                alreadySent -> "Đã gửi lời mời"
                isFriend -> "Bạn bè"
                else -> "Gửi lời mời"
            })
        }

    }
}



@Composable
fun FriendItem(
    friend: User,
    onRemoveFriend: (User) -> Unit // Thêm tham số onRemoveFriend với đối số là User
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = friend.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { showConfirmationDialog = true },
            modifier = Modifier.size(36.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Xóa bạn bè",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa '${friend.name}' khỏi bạn bè không?",
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFriend(friend) // Gọi hàm xóa bạn bè và truyền friend vào
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Chắc chắn", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Không")
                }
            }
        )
    }
}



@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${request.fromName} muốn kết bạn với bạn",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                Text(text = "Chấp nhận")
            }
            Button(onClick = onDecline, modifier = Modifier.weight(1f)) {
                Text(text = "Từ chối")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    val navController = rememberNavController()
    FindFriendsScreen(navController)
}

