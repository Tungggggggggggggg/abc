package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileHeader(
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
            text = "Thông tin cá nhân",
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

@Composable
fun EditableProfileInfoRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userData: Map<String, Any>? = null,
    isEditing: Boolean = false,
    description: String = "",
    onDescriptionChange: (String) -> Unit = {},
    onEditClick: () -> Unit = {},
    onSaveClick: (String?) -> Unit = { _ -> },
    onMatchHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var editableName by remember { mutableStateOf(userData?.get("name")?.toString() ?: "") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c97c5d))
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Ảnh đại diện",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (isEditing) {
                        onSaveClick(editableName)
                    } else {
                        onEditClick()
                    }
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isEditing) "Lưu" else "Sửa thông tin",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { /* TODO: Xử lý đổi ảnh */ },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Đổi ảnh",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onMatchHistoryClick,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Lịch sử trận đấu",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .border(1.dp, Color.Black)
                .background(colorResource(id = R.color.color_eee2df))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                EditableProfileInfoRow(label = "Tên:", value = editableName, onValueChange = { editableName = it })
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Email:", value = FirebaseAuth.getInstance().currentUser?.email ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Ngày tạo:", value = userData?.get("createdAt")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Điểm:", value = userData?.get("score")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mô tả:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.width(100.dp)
                            )
                            innerTextField()
                        }
                    }
                )
            } else {
                ProfileInfoRow(label = "Tên:", value = userData?.get("name")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Email:", value = FirebaseAuth.getInstance().currentUser?.email ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Ngày tạo:", value = userData?.get("createdAt")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Điểm:", value = userData?.get("score")?.toString() ?: "")
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                ProfileInfoRow(label = "Mô tả:", value = description)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProfileScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() },
    onMatchHistoryClick: () -> Unit = { navController?.navigate("match_history") },
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isFetchingData by remember { mutableStateOf(true) }

    // State để lưu trữ tên có thể chỉnh sửa
    var editableName by remember { mutableStateOf("") }

    LaunchedEffect(userData) {
        editableName = userData?.get("name")?.toString() ?: ""
    }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.data
                        description = document.getString("description") ?: ""
                        editableName = document.getString("name") ?: "" // Khởi tạo editableName
                    }
                    isFetchingData = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi khi tải thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                    isFetchingData = false
                }
        } else {
            navController?.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            isFetchingData = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeader(onBackClick = onBackClick)
        if (isFetchingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ProfileContent(
                modifier = Modifier.fillMaxWidth().weight(1f),
                userData = userData,
                isEditing = isEditing,
                description = description,
                onDescriptionChange = { description = it },
                onEditClick = {
                    isEditing = true
                    editableName =
                        userData?.get("name")?.toString() ?: "" // Cập nhật khi bắt đầu chỉnh sửa
                },
                onSaveClick = { currentEditableName ->
                    if (isEditing) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            // Cập nhật tên trong Firestore
                            if (userData?.get("name")?.toString() != currentEditableName && currentEditableName != null) {
                                firestore.collection("users")
                                    .document(userId)
                                    .update("name", currentEditableName)
                                    .addOnSuccessListener {
                                        userData = userData?.toMutableMap()?.apply { put("name", currentEditableName) }
                                        Toast.makeText(context, "Đã cập nhật tên.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Lỗi khi cập nhật tên: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // Lưu mô tả
                            firestore.collection("users")
                                .document(userId)
                                .update("description", description)
                                .addOnSuccessListener {
                                    isEditing = false
                                    Toast.makeText(context, "Đã lưu thông tin.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi khi lưu mô tả: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                onMatchHistoryClick = onMatchHistoryClick,
                onLogoutClick = {
                    auth.signOut()
                    Toast.makeText(context, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    navController?.navigate("home") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ChessmateTheme {
        ProfileScreen()
    }
}