package com.example.chessmate.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.model.FriendRequest
import com.example.chessmate.model.User
import com.example.chessmate.viewmodel.FindFriendsViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.graphicsLayer

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
                .padding(horizontal = 16.dp)
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        onSearch()
                        true
                    } else {
                        false
                    }
                },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "Nhập tên người chơi ...",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
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
    var currentSearchQuery by remember { mutableStateOf("") }
    var isSearchEmpty by remember { mutableStateOf(false) }
    var isSearchResultsExpanded by remember { mutableStateOf(true) }
    var isFriendRequestsExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadReceivedRequests()
        viewModel.loadSentRequests()
        viewModel.loadFriends()
    }

    // Sắp xếp searchResults: bạn bè -> đã gửi lời mời -> còn lại
    val sortedSearchResults = searchResults.sortedWith(compareBy<User> {
        when {
            friends.any { friend -> friend.userId == it.userId } -> 0
            it.userId in sentRequests -> 1
            else -> 2
        }
    })

    // Đồng bộ hóa isSearchEmpty với kết quả tìm kiếm
    LaunchedEffect(searchResults, currentSearchQuery) {
        if (searchQuery == currentSearchQuery) {
            isSearchEmpty = searchQuery.isNotBlank() && searchResults.isEmpty()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(navController = navController)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFC97C5D))
                .padding(16.dp)
        ) {
            BackButton(onBackClick = {
                navController.navigate("main_screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            })
            Logo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Spacer(modifier = Modifier.height(20.dp))

            SearchBar(
                text = searchQuery,
                onTextChanged = { searchQuery = it },
                onSearch = {
                    currentSearchQuery = searchQuery
                    viewModel.searchUsers(searchQuery)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isSearchEmpty && searchQuery.isNotBlank()) {
                Text(
                    text = "Người dùng không tồn tại!",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            } else if (sortedSearchResults.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSearchResultsExpanded = !isSearchResultsExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kết quả tìm kiếm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand/Collapse",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = if (isSearchResultsExpanded) 0f else 180f }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isSearchResultsExpanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = (5 * 48).dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedSearchResults) { user ->
                            val isFriend = friends.any { it.userId == user.userId }
                            SearchResultItem(
                                user = user,
                                alreadySent = user.userId in sentRequests,
                                onAddFriend = {
                                    viewModel.sendFriendRequest(user.userId)
                                    Toast.makeText(context, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                                },
                                onCancelFriendRequest = {
                                    viewModel.cancelFriendRequest(user.userId)
                                    Toast.makeText(context, "Đã hủy lời mời kết bạn!", Toast.LENGTH_SHORT).show()
                                },
                                isFriend = isFriend,
                                onProfileClick = {
                                    navController.navigate("competitor_profile/${user.userId}")
                                }
                            )
                        }
                    }
                }
            }

            if (receivedRequests.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFriendRequestsExpanded = !isFriendRequestsExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lời mời kết bạn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand/Collapse",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = if (isFriendRequestsExpanded) 0f else 180f }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isFriendRequestsExpanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = (5 * 48).dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(receivedRequests) { request ->
                            FriendRequestItem(
                                request = request,
                                onAccept = {
                                    viewModel.acceptFriendRequest(request)
                                    Toast.makeText(context, "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show()
                                },
                                onDecline = {
                                    viewModel.declineFriendRequest(request)
                                    Toast.makeText(context, "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show()
                                },
                                onProfileClick = {
                                    navController.navigate("competitor_profile/${request.fromUserId}")
                                }
                            )
                        }
                    }
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
    onCancelFriendRequest: () -> Unit,
    isFriend: Boolean,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c), shape = RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = user.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        )
        if (!alreadySent && !isFriend) {
            Button(
                onClick = onAddFriend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.color_eed7c5),
                    contentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Gửi lời mời")
            }
        } else if (isFriend) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF388E3C),
                    disabledContentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Bạn bè")
            }
        } else {
            // Hiển thị nút "Xóa lời mời"
            Button(
                onClick = onCancelFriendRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.color_b36a5e),
                    contentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Xóa lời mời")
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = request.fromName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        )
        IconButton(
            onClick = onAccept,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.check),
                contentDescription = "Chấp nhận",
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(
            onClick = onDecline,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Từ chối",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    val navController = rememberNavController()
    FindFriendsScreen(navController)
}