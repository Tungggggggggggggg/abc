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
            "status" to "waiting"
        )

        viewModelScope.launch {
            try {
                db.collection("matchmaking_queue")
                    .document(userId)
                    .set(queueData)
                    .await()
                tryMatchmaking(userId)
            } catch (e: Exception) {
                matchmakingError.value = "Không thể tham gia hàng đợi: ${e.message}"
                isMatchmaking.set(false)
            }
        }
    }

    private suspend fun tryMatchmaking(userId: String) {
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            val timeoutSeconds = 60L // Tăng thời gian chờ lên 60 giây
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
                val matched = tryMatchWithOpponent(userId)
                if (matched) {
                    isMatchmaking.set(false)
                    return@launch
                }
                delay(2000L) // Giãn cách kiểm tra lên 2 giây để giảm tải
            }

            db.collection("matchmaking_queue").document(userId).delete()
            matchmakingError.value = "Không tìm thấy đối thủ trong $timeoutSeconds giây. Vui lòng thử lại."
            isMatchmaking.set(false)
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

            if (waitingPlayers.isEmpty()) {
                return false
            }

            val opponentDoc = waitingPlayers.first()
            val opponentId = opponentDoc.getString("userId") ?: return false

            val success = db.runTransaction { transaction ->
                val userRef = db.collection("matchmaking_queue").document(userId)
                val opponentRef = db.collection("matchmaking_queue").document(opponentId)

                val userDoc = transaction.get(userRef)
                val opponentDoc = transaction.get(opponentRef)

                // Kiểm tra trạng thái của cả hai người chơi
                if (userDoc.exists() && opponentDoc.exists() &&
                    userDoc.getString("status") == "waiting" &&
                    opponentDoc.getString("status") == "waiting"
                ) {
                    transaction.update(userRef, "status", "matched")
                    transaction.update(opponentRef, "status", "matched")
                    true
                } else {
                    false
                }
            }.await()

            if (success) {
                createMatch(userId, opponentId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            matchmakingError.value = "Lỗi khi ghép cặp: ${e.message}"
            false
        }
    }

    private fun createMatch(player1Id: String, player2Id: String) {
        val matchId = db.collection("matches").document().id
        this.matchId.value = matchId

        val matchData = hashMapOf(
            "matchId" to matchId,
            "player1" to player1Id,
            "player2" to player2Id,
            "board" to boardToMap(game.getBoard()),
            "currentTurn" to currentTurn.value.toString(),
            "whiteTime" to 600,
            "blackTime" to 600,
            "status" to "ongoing",
            "winner" to null,
            "drawRequest" to null,
            "lastMove" to null
        )

        viewModelScope.launch {
            try {
                db.collection("matches")
                    .document(matchId)
                    .set(matchData)
                    .await()

                db.collection("matchmaking_queue").document(player1Id).delete()
                db.collection("matchmaking_queue").document(player2Id).delete()

                playerColor.value = if (auth.currentUser?.uid == player1Id) PieceColor.WHITE else PieceColor.BLACK
                listenToMatchUpdates()
                startTimer()
            } catch (e: Exception) {
                matchmakingError.value = "Lỗi khi tạo trận đấu: ${e.message}"
            }
        }
    }

    fun listenToMatchUpdates() {
        matchId.value?.let { id ->
            matchListener = db.collection("matches")
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val matchDataValue = snapshot.data ?: return@addSnapshotListener
                    matchData.value = matchDataValue

                    val boardData = matchDataValue["board"] as? List<List<Map<String, Any?>>>
                    val currentTurnStr = matchDataValue["currentTurn"] as? String
                    val whiteTimeData = matchDataValue["whiteTime"] as? Long
                    val blackTimeData = matchDataValue["blackTime"] as? Long
                    val status = matchDataValue["status"] as? String
                    val winner = matchDataValue["winner"] as? String
                    val drawRequestData = matchDataValue["drawRequest"] as? String

                    if (boardData != null) {
                        board.value = mapToBoard(boardData)
                    }

                    currentTurn.value = PieceColor.valueOf(currentTurnStr ?: "WHITE")
                    whiteTime.value = (whiteTimeData ?: 600).toInt()
                    blackTime.value = (blackTimeData ?: 600).toInt()
                    drawRequest.value = drawRequestData

                    if (status != "ongoing") {
                        isGameOver.value = true
                        gameResult.value = when (status) {
                            "draw" -> "Ván đấu hòa theo thỏa thuận."
                            "surrendered" -> if (winner == auth.currentUser?.uid) {
                                "Bạn thắng vì đối thủ đầu hàng! (+10 điểm)"
                            } else {
                                "Đối thủ thắng vì bạn đầu hàng! (-5 điểm)"
                            }
                            "checkmate" -> if (winner == auth.currentUser?.uid) {
                                "Bạn thắng vì chiếu hết! (+10 điểm)"
                            } else {
                                "Đối thủ thắng vì chiếu hết! (-5 điểm)"
                            }
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
        if (isPromoting.value || playerColor.value != currentTurn.value) return

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

            // Cập nhật điểm số
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
                                "draw" -> {
                                    // Không thay đổi điểm số khi hòa
                                }
                                "surrendered", "checkmate" -> {
                                    if (winner == player1Id) {
                                        transaction.update(player1Ref, "score", player1Score + 10)
                                        transaction.update(player2Ref, "score", (player2Score - 5).coerceAtLeast(0))
                                    } else if (winner == player2Id) {
                                        transaction.update(player2Ref, "score", player2Score + 10)
                                        transaction.update(player1Ref, "score", (player1Score - 5).coerceAtLeast(0))
                                    } else {
                                        // Trường hợp winner không hợp lệ
                                    }
                                }
                                else -> {
                                    // Xử lý trạng thái không xác định
                                }
                            }
                        }.await()
                    }
                } catch (e: Exception) {
                    // Xử lý lỗi nếu cần
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
                        "board" to boardToMap(board.value),
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

    private fun boardToMap(board: Array<Array<ChessPiece?>>): List<List<Map<String, Any?>>> {
        return board.map { row ->
            row.map { piece ->
                if (piece == null) {
                    mapOf("type" to null, "color" to null, "position" to null)
                } else {
                    mapOf(
                        "type" to piece.type.toString(),
                        "color" to piece.color.toString(),
                        "position" to mapOf("row" to piece.position.row, "col" to piece.position.col)
                    )
                }
            }
        }
    }

    private fun mapToBoard(boardData: List<List<Map<String, Any?>>>): Array<Array<ChessPiece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                val pieceData = boardData[row][col]
                val type = pieceData["type"]?.toString()
                val color = pieceData["color"]?.toString()
                val positionData = pieceData["position"] as? Map<String, Long>
                if (type != null && color != null && positionData != null) {
                    ChessPiece(
                        type = PieceType.valueOf(type),
                        color = PieceColor.valueOf(color),
                        position = Position(positionData["row"]!!.toInt(), positionData["col"]!!.toInt())
                    )
                } else {
                    null
                }
            }
        }
    }

    fun getPendingPromotion(): Position? = game.getPendingPromotion()

    fun cancelMatchmaking() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("matchmaking_queue").document(userId).delete()
        }
        matchmakingJob?.cancel()
        isMatchmaking.set(false)
        matchmakingError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchListener?.remove()
        matchId.value?.let { id ->
            db.collection("matches").document(id).delete()
        }
        cancelMatchmaking()
    }
}