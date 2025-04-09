package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.ChessGame
import com.example.chessmate.model.ChessPiece
import com.example.chessmate.model.Move
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType
import com.example.chessmate.model.Position
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class OnlineChessViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)
    val whiteTime = mutableStateOf(600)
    val blackTime = mutableStateOf(600)
    val isPromoting = mutableStateOf(false)
    val matchId = mutableStateOf<String?>(null)
    val playerColor = mutableStateOf<PieceColor?>(null)
    val drawRequest = mutableStateOf<String?>(null)
    private val matchData = mutableStateOf<Map<String, Any>?>(null)
    val matchmakingError = mutableStateOf<String?>(null)

    private var timerJob: Job? = null
    private var matchListener: ListenerRegistration? = null
    private var matchmakingListener: ListenerRegistration? = null
    private var matchmakingJob: Job? = null
    private val isMatchmaking = AtomicBoolean(false)

    init {
        startMatchmaking()
    }

    fun startMatchmaking() {
        val userId = auth.currentUser?.uid ?: run {
            matchmakingError.value = "Vui lòng đăng nhập để chơi trực tuyến."
            return
        }
        if (isMatchmaking.get()) return

        isMatchmaking.set(true)
        val queueData = hashMapOf(
            "userId" to userId,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "waiting",
            "matchId" to null
        )

        viewModelScope.launch {
            try {
                db.collection("matchmaking_queue")
                    .document(userId)
                    .set(queueData)
                    .await()
                listenForMatchmaking(userId)
            } catch (e: Exception) {
                matchmakingError.value = "Không thể tham gia hàng đợi: ${e.message}"
                isMatchmaking.set(false)
                db.collection("matchmaking_queue").document(userId).delete()
            }
        }
    }

    private fun listenForMatchmaking(userId: String) {
        matchmakingListener?.remove()
        matchmakingListener = db.collection("matchmaking_queue")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    matchmakingError.value = "Lỗi nghe hàng đợi: ${error?.message}"
                    isMatchmaking.set(false)
                    return@addSnapshotListener
                }

                val status = snapshot.getString("status")
                val matchIdFromQueue = snapshot.getString("matchId")

                if (status == "matched" && matchIdFromQueue != null) {
                    matchId.value = matchIdFromQueue
                    playerColor.value = if (auth.currentUser?.uid == snapshot.getString("player1")) PieceColor.WHITE else PieceColor.BLACK
                    listenToMatchUpdates()
                    startTimer()
                    db.collection("matchmaking_queue").document(userId).delete()
                    isMatchmaking.set(false)
                    matchmakingListener?.remove()
                } else if (status == "waiting") {
                    viewModelScope.launch {
                        tryMatchmaking(userId)
                    }
                }
            }
    }

    private suspend fun tryMatchmaking(userId: String) {
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            val timeoutSeconds = 60L
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000 && isMatchmaking.get()) {
                val matched = tryMatchWithOpponent(userId)
                if (matched) return@launch
                delay(2000L)
            }

            if (isMatchmaking.get()) {
                db.collection("matchmaking_queue").document(userId).delete()
                matchmakingError.value = "Không tìm thấy đối thủ trong $timeoutSeconds giây."
                isMatchmaking.set(false)
            }
        }
    }

    private suspend fun tryMatchWithOpponent(userId: String): Boolean {
        return try {
            val snapshot = db.collection("matchmaking_queue")
                .whereEqualTo("status", "waiting")
                .get()
                .await()

            val waitingPlayers = snapshot.documents
                .filter { it.getString("userId") != userId }
                .sortedBy { it.getTimestamp("timestamp")?.toDate()?.time }

            if (waitingPlayers.isEmpty()) return false

            val opponentDoc = waitingPlayers.first()
            val opponentId = opponentDoc.getString("userId") ?: return false

            val matchId = db.collection("matches").document().id
            val matchData = hashMapOf(
                "matchId" to matchId,
                "player1" to userId,
                "player2" to opponentId,
                "board" to boardToFlatMap(game.getBoard()), // Sử dụng định dạng phẳng
                "currentTurn" to currentTurn.value.toString(),
                "whiteTime" to 600,
                "blackTime" to 600,
                "status" to "ongoing",
                "winner" to null,
                "drawRequest" to null,
                "lastMove" to null
            )

            val success = db.runTransaction { transaction ->
                val userRef = db.collection("matchmaking_queue").document(userId)
                val opponentRef = db.collection("matchmaking_queue").document(opponentId)

                val userDoc = transaction.get(userRef)
                val opponentDoc = transaction.get(opponentRef)

                if (userDoc.exists() && opponentDoc.exists() &&
                    userDoc.getString("status") == "waiting" &&
                    opponentDoc.getString("status") == "waiting"
                ) {
                    transaction.set(db.collection("matches").document(matchId), matchData)
                    transaction.update(userRef, mapOf("status" to "matched", "matchId" to matchId))
                    transaction.update(opponentRef, mapOf("status" to "matched", "matchId" to matchId))
                    true
                } else {
                    false
                }
            }.await()

            if (success) {
                this.matchId.value = matchId
                playerColor.value = PieceColor.WHITE
                listenToMatchUpdates()
                startTimer()
                return true
            }
            false
        } catch (e: Exception) {
            matchmakingError.value = "Lỗi khi ghép cặp: ${e.message}"
            db.collection("matchmaking_queue").document(userId).delete()
            return false
        }
    }

    fun listenToMatchUpdates() {
        matchId.value?.let { id ->
            matchListener?.remove()
            matchListener = db.collection("matches")
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        matchmakingError.value = "Lỗi nghe trận đấu: ${error?.message}"
                        return@addSnapshotListener
                    }

                    val matchDataValue = snapshot.data ?: return@addSnapshotListener
                    matchData.value = matchDataValue

                    val boardData = matchDataValue["board"] as? List<Map<String, Any?>>
                    val currentTurnStr = matchDataValue["currentTurn"] as? String
                    val whiteTimeData = matchDataValue["whiteTime"] as? Long
                    val blackTimeData = matchDataValue["blackTime"] as? Long
                    val status = matchDataValue["status"] as? String
                    val winner = matchDataValue["winner"] as? String
                    val drawRequestData = matchDataValue["drawRequest"] as? String

                    if (boardData != null) {
                        board.value = flatMapToBoard(boardData)
                    }
                    currentTurn.value = PieceColor.valueOf(currentTurnStr ?: "WHITE")
                    whiteTime.value = (whiteTimeData ?: 600).toInt()
                    blackTime.value = (blackTimeData ?: 600).toInt()
                    drawRequest.value = drawRequestData

                    if (playerColor.value == null) {
                        playerColor.value = if (auth.currentUser?.uid == matchDataValue["player1"]) PieceColor.WHITE else PieceColor.BLACK
                    }

                    if (status != "ongoing") {
                        isGameOver.value = true
                        gameResult.value = when (status) {
                            "draw" -> "Ván đấu hòa."
                            "surrendered" -> if (winner == auth.currentUser?.uid) "Bạn thắng!" else "Bạn thua!"
                            "checkmate" -> if (winner == auth.currentUser?.uid) "Bạn thắng!" else "Bạn thua!"
                            else -> "Trò chơi kết thúc."
                        }
                        timerJob?.cancel()
                    }
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!isGameOver.value && !isPromoting.value) {
                delay(1000L)
                if (currentTurn.value == PieceColor.WHITE) {
                    whiteTime.value = (whiteTime.value - 1).coerceAtLeast(0)
                    updateTime("whiteTime", whiteTime.value)
                    if (whiteTime.value == 0) {
                        endMatch(status = "checkmate", winner = matchData.value?.get("player2") as? String)
                        break
                    }
                } else {
                    blackTime.value = (blackTime.value - 1).coerceAtLeast(0)
                    updateTime("blackTime", blackTime.value)
                    if (blackTime.value == 0) {
                        endMatch(status = "checkmate", winner = matchData.value?.get("player1") as? String)
                        break
                    }
                }
            }
        }
    }

    private fun updateTime(field: String, time: Int) {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(field, time)
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value || playerColor.value != currentTurn.value || matchId.value == null) return

        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (game.getPendingPromotion() != null) {
                    isPromoting.value = true
                    timerJob?.cancel()
                } else {
                    startTimer()
                }
            }
        } else {
            highlightedSquares.value = game.selectPiece(row, col)
        }
    }

    fun promotePawn(toType: PieceType) {
        game.promotePawn(toType)
        isPromoting.value = false
        updateGameState()
        startTimer()
    }

    fun requestDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update("drawRequest", auth.currentUser?.uid)
        }
    }

    fun acceptDraw() {
        endMatch(status = "draw", winner = null)
    }

    fun declineDraw() {
        matchId.value?.let { id ->
            db.collection("matchmaking_queue").document(id).delete()
            db.collection("matches")
                .document(id)
                .update("drawRequest", null)
        }
    }

    fun surrender() {
        val opponentId = if (playerColor.value == PieceColor.WHITE) {
            matchData.value?.get("player2") as? String
        } else {
            matchData.value?.get("player1") as? String
        }
        endMatch(status = "surrendered", winner = opponentId)
    }

    private fun endMatch(status: String, winner: String?) {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "status" to status,
                        "winner" to winner
                    )
                )

            viewModelScope.launch {
                try {
                    val player1Id = matchData.value?.get("player1") as? String
                    val player2Id = matchData.value?.get("player2") as? String
                    if (player1Id != null && player2Id != null) {
                        db.runTransaction { transaction ->
                            val player1Ref = db.collection("users").document(player1Id)
                            val player2Ref = db.collection("users").document(player2Id)

                            val player1Doc = transaction.get(player1Ref)
                            val player2Doc = transaction.get(player2Ref)

                            val player1Score = (player1Doc.getLong("score") ?: 0).toInt()
                            val player2Score = (player2Doc.getLong("score") ?: 0).toInt()

                            when (status) {
                                "draw" -> {}
                                "surrendered", "checkmate" -> {
                                    if (winner == player1Id) {
                                        transaction.update(player1Ref, "score", player1Score + 10)
                                        transaction.update(player2Ref, "score", (player2Score - 5).coerceAtLeast(0))
                                    } else if (winner == player2Id) {
                                        transaction.update(player2Ref, "score", player2Score + 10)
                                        transaction.update(player1Ref, "score", (player1Score - 5).coerceAtLeast(0))
                                    } else {
                                        // Trường hợp winner là null hoặc không hợp lệ
                                    }
                                }
                                else -> {}
                            }
                        }.await()
                    }
                } catch (e: Exception) {
                    matchmakingError.value = "Lỗi cập nhật điểm: ${e.message}"
                }
            }
        }
    }

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()

        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "board" to boardToFlatMap(board.value),
                        "currentTurn" to currentTurn.value.toString(),
                        "lastMove" to game.getLastMove()?.let { lastMove ->
                            mapOf(
                                "from" to mapOf("row" to lastMove.first.row, "col" to lastMove.first.col),
                                "to" to mapOf("row" to lastMove.second.row, "col" to lastMove.second.col)
                            )
                        }
                    )
                )
        }
    }

    // Chuyển bàn cờ thành danh sách phẳng
    private fun boardToFlatMap(board: Array<Array<ChessPiece?>>): List<Map<String, Any?>> {
        val flatList = mutableListOf<Map<String, Any?>>()
        for (row in board.indices) {
            for (col in board[row].indices) {
                val piece = board[row][col]
                if (piece != null) {
                    flatList.add(
                        mapOf(
                            "type" to piece.type.toString(),
                            "color" to piece.color.toString(),
                            "position" to mapOf(
                                "row" to row,
                                "col" to col
                            )
                        )
                    )
                }
            }
        }
        return flatList
    }

    // Chuyển từ danh sách phẳng về bàn cờ
    private fun flatMapToBoard(boardData: List<Map<String, Any?>>): Array<Array<ChessPiece?>> {
        val newBoard = Array(8) { Array<ChessPiece?>(8) { null } }
        for (pieceData in boardData) {
            val type = pieceData["type"]?.toString()
            val color = pieceData["color"]?.toString()
            val positionData = pieceData["position"] as? Map<String, Long>
            if (type != null && color != null && positionData != null) {
                val row = positionData["row"]?.toInt() ?: continue
                val col = positionData["col"]?.toInt() ?: continue
                if (row in 0..7 && col in 0..7) {
                    newBoard[row][col] = ChessPiece(
                        type = PieceType.valueOf(type),
                        color = PieceColor.valueOf(color),
                        position = Position(row, col)
                    )
                }
            }
        }
        return newBoard
    }

    fun cancelMatchmaking() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("matchmaking_queue").document(userId).delete()
        }
        matchmakingJob?.cancel()
        matchmakingListener?.remove()
        isMatchmaking.set(false)
        matchmakingError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchListener?.remove()
        matchmakingListener?.remove()
        matchId.value?.let { id ->
            db.collection("matches").document(id).delete()
        }
        cancelMatchmaking()
    }
}