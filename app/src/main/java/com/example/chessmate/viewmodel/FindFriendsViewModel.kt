package com.example.chessmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chessmate.model.FriendRequest
import com.example.chessmate.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
        if (query.isBlank()) {
            println("Search query is blank, returning empty results")
            _searchResults.value = emptyList()
            return
        }
        val queryLowercase = query.lowercase().trim()
        println("Searching for query: '$queryLowercase'")

        firestore.collection("users")
            .whereArrayContains("nameKeywords", queryLowercase)
            .get()
            .addOnSuccessListener { result ->
                println("Firestore query returned ${result.documents.size} documents")
                val users = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: return@mapNotNull null
                    val userId = doc.getString("userId") ?: doc.id
                    val nameKeywords = doc.get("nameKeywords") as? List<String> ?: emptyList()
                    val currentUid = auth.currentUser?.uid
                    if (userId == currentUid) return@mapNotNull null
                    println("Found user: $userId, name: $name, nameKeywords: $nameKeywords")
                    User(userId, name, email)
                }
                println("Search results: ${users.size} users found")
                _searchResults.value = users
            }
            .addOnFailureListener { exception ->
                println("Search failed: ${exception.message}")
                _searchResults.value = emptyList()
            }
    }

    fun sendFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid ?: return
        val fromUserName = auth.currentUser?.displayName ?: "Unknown"

        val request = hashMapOf(
            "fromUserId" to fromUserId,
            "fromName" to fromUserName,
            "toUserId" to toUserId,
            "timestamp" to Timestamp.now(),
            "status" to "pending"
        )

        firestore.collection("friend_requests")
            .add(request)
            .addOnSuccessListener {
                _sentRequests.value = _sentRequests.value + toUserId
            }
    }

    fun cancelFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid ?: return
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", fromUserId)
            .whereEqualTo("toUserId", toUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                requestDoc?.reference?.delete()?.addOnSuccessListener {
                    _sentRequests.value = _sentRequests.value.filter { it != toUserId }
                }
            }
    }

    fun removeFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("friends")
            .whereEqualTo("user1", currentUserId)
            .whereEqualTo("user2", friendId)
            .get()
            .addOnSuccessListener { result1 ->
                val doc1 = result1.documents.firstOrNull()
                if (doc1 != null) {
                    doc1.reference.delete().addOnSuccessListener {
                        _friends.value = _friends.value.filter { it.userId != friendId }
                    }
                } else {
                    firestore.collection("friends")
                        .whereEqualTo("user1", friendId)
                        .whereEqualTo("user2", currentUserId)
                        .get()
                        .addOnSuccessListener { result2 ->
                            val doc2 = result2.documents.firstOrNull()
                            doc2?.reference?.delete()?.addOnSuccessListener {
                                _friends.value = _friends.value.filter { it.userId != friendId }
                            }
                        }
                }
            }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        firestore.collection("friend_requests")
            .whereEqualTo("fromUserId", request.fromUserId)
            .whereEqualTo("toUserId", request.toUserId)
            .get()
            .addOnSuccessListener { result ->
                val requestDoc = result.documents.firstOrNull()
                if (requestDoc != null) {
                    requestDoc.reference.update("status", "accepted")
                        .addOnSuccessListener {
                            firestore.collection("friends")
                                .add(
                                    hashMapOf(
                                        "user1" to request.fromUserId,
                                        "user2" to request.toUserId
                                    )
                                )
                                .addOnSuccessListener {
                                    _friends.value = _friends.value + User(
                                        request.fromUserId,
                                        request.fromName,
                                        ""
                                    )
                                    loadReceivedRequests()
                                    loadSentRequests()
                                    loadFriends()
                                }
                            requestDoc.reference.delete()
                                .addOnSuccessListener {
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
                    val fromName = doc.getString("fromName") ?: "Unknown"
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