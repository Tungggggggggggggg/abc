package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val board = mutableStateOf(Array(8) { Array<ChessPiece?>(8) { null } })
    val currentTurn = mutableStateOf(PieceColor.WHITE)
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(false)
    val gameResult = mutableStateOf<String?>(null)
    val whiteTime = mutableStateOf(600)
    val blackTime = mutableStateOf(600)
    val isPromoting = mutableStateOf(false)
    val matchId = mutableStateOf<String?>(null)
    val playerColor = mutableStateOf<PieceColor?>(null)
    val drawRequest = mutableStateOf<String?>(null)
    val moveHistory = mutableStateListOf<String>()
    private val matchData = mutableStateOf<Map<String, Any>?>(null)
    val matchmakingError = mutableStateOf<String?>(null)

    // Thêm các biến để kiểm tra luật hòa
    private val positionHistory = mutableMapOf<String, Int>()
    private var fiftyMoveCounter = 0
    private var whiteKingPosition = Position(0, 4)
    private var blackKingPosition = Position(7, 4)
    private var lastMove: Pair<Position, Position>? = null

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
                    viewModelScope.launch {
                        val matchDoc = db.collection("matches").document(matchIdFromQueue).get().await()
                        val player1Id = matchDoc.getString("player1")
                        playerColor.value = if (auth.currentUser?.uid == player1Id) PieceColor.WHITE else PieceColor.BLACK
                        listenToMatchUpdates()
                        startTimer()
                    }
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
            val initialBoard = createInitialBoard()
            val matchData = hashMapOf(
                "matchId" to matchId,
                "player1" to userId,
                "player2" to opponentId,
                "board" to boardToFlatMap(initialBoard),
                "currentTurn" to PieceColor.WHITE.toString(),
                "whiteTime" to 600,
                "blackTime" to 600,
                "status" to "ongoing",
                "winner" to null,
                "drawRequest" to null,
                "lastMove" to null,
                "moveHistory" to emptyList<String>(),
                "fiftyMoveCounter" to 0
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
                    val moveHistoryData = matchDataValue["moveHistory"] as? List<String>
                    val lastMoveData = matchDataValue["lastMove"] as? Map<String, Map<String, Long>>
                    val fiftyMoveCounterData = matchDataValue["fiftyMoveCounter"] as? Long

                    if (boardData != null) {
                        board.value = flatMapToBoard(boardData)
                    }
                    currentTurn.value = PieceColor.valueOf(currentTurnStr ?: "WHITE")
                    whiteTime.value = (whiteTimeData ?: 600).toInt()
                    blackTime.value = (blackTimeData ?: 600).toInt()
                    drawRequest.value = drawRequestData
                    moveHistory.clear()
                    moveHistory.addAll(moveHistoryData ?: emptyList())
                    fiftyMoveCounter = (fiftyMoveCounterData ?: 0).toInt()

                    if (lastMoveData != null) {
                        val from = lastMoveData["from"]?.let { Position(it["row"]!!.toInt(), it["col"]!!.toInt()) }
                        val to = lastMoveData["to"]?.let { Position(it["row"]!!.toInt(), it["col"]!!.toInt()) }
                        if (from != null && to != null) {
                            lastMove = Pair(from, to)
                        }
                    }

                    if (playerColor.value == null) {
                        playerColor.value = if (auth.currentUser?.uid == matchDataValue["player1"]) PieceColor.WHITE else PieceColor.BLACK
                    }

                    // Cập nhật vị trí vua
                    updateKingPositions()

                    // Kiểm tra trạng thái game
                    checkGameState()

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
                matchId.value?.let { id ->
                    db.runTransaction { transaction ->
                        val matchRef = db.collection("matches").document(id)
                        val snapshot = transaction.get(matchRef)
                        val currentTurnStr = snapshot.getString("currentTurn") ?: "WHITE"
                        val currentTurn = PieceColor.valueOf(currentTurnStr)
                        var whiteTime = (snapshot.getLong("whiteTime") ?: 600).toInt()
                        var blackTime = (snapshot.getLong("blackTime") ?: 600).toInt()

                        if (currentTurn == PieceColor.WHITE) {
                            whiteTime = (whiteTime - 1).coerceAtLeast(0)
                            transaction.update(matchRef, "whiteTime", whiteTime)
                            if (whiteTime == 0) {
                                transaction.update(
                                    matchRef,
                                    mapOf(
                                        "status" to "checkmate",
                                        "winner" to snapshot.getString("player2")
                                    )
                                )
                            }
                        } else {
                            blackTime = (blackTime - 1).coerceAtLeast(0)
                            transaction.update(matchRef, "blackTime", blackTime)
                            if (blackTime == 0) {
                                transaction.update(
                                    matchRef,
                                    mapOf(
                                        "status" to "checkmate",
                                        "winner" to snapshot.getString("player1")
                                    )
                                )
                            }
                        }
                    }.await()
                }
            }
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value || playerColor.value != currentTurn.value || matchId.value == null) return

        val position = Position(row, col)
        val selectedMove = highlightedSquares.value.find { it.position == position }
        if (selectedMove != null) {
            val fromPosition = selectedMove.from
            val toPosition = selectedMove.position
            val piece = board.value[fromPosition.row][fromPosition.col]
            if (piece != null) {
                val targetPiece = board.value[toPosition.row][toPosition.col]
                board.value[fromPosition.row][fromPosition.col] = null
                board.value[toPosition.row][toPosition.col] = piece.copy(position = toPosition)

                // Kiểm tra phong cấp
                if (piece.type == PieceType.PAWN && (toPosition.row == 0 || toPosition.row == 7)) {
                    isPromoting.value = true
                    timerJob?.cancel()
                } else {
                    currentTurn.value = if (currentTurn.value == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                    startTimer()
                }

                // Cập nhật lịch sử nước đi
                val moveNotation = "${piece.type} từ ${positionToString(fromPosition)} đến ${positionToString(toPosition)}"
                moveHistory.add(moveNotation)

                // Cập nhật fiftyMoveCounter
                if (piece.type == PieceType.PAWN || targetPiece != null) {
                    fiftyMoveCounter = 0
                } else {
                    fiftyMoveCounter++
                }

                // Cập nhật lastMove
                lastMove = Pair(fromPosition, toPosition)

                // Lưu trạng thái bàn cờ để kiểm tra lặp lại vị trí
                saveBoardState()

                // Cập nhật Firestore
                matchId.value?.let { id ->
                    db.collection("matches")
                        .document(id)
                        .update(
                            mapOf(
                                "board" to boardToFlatMap(board.value),
                                "currentTurn" to currentTurn.value.toString(),
                                "lastMove" to mapOf(
                                    "from" to mapOf("row" to fromPosition.row, "col" to fromPosition.col),
                                    "to" to mapOf("row" to toPosition.row, "col" to toPosition.col)
                                ),
                                "moveHistory" to moveHistory.toList(),
                                "fiftyMoveCounter" to fiftyMoveCounter
                            )
                        )
                }
            }
            highlightedSquares.value = emptyList()
        } else {
            val piece = board.value[row][col]
            if (piece != null && piece.color == playerColor.value) {
                highlightedSquares.value = getPossibleMoves(piece, Position(row, col))
            } else {
                highlightedSquares.value = emptyList()
            }
        }
    }

    fun promotePawn(toType: PieceType) {
        val pawnPosition = board.value.flatMapIndexed { row, cols ->
            cols.mapIndexedNotNull { col, piece ->
                if (piece?.type == PieceType.PAWN && (row == 0 || row == 7)) Position(row, col) else null
            }
        }.firstOrNull()

        pawnPosition?.let { pos ->
            val piece = board.value[pos.row][pos.col]
            if (piece != null) {
                board.value[pos.row][pos.col] = piece.copy(type = toType)
            }
        }

        isPromoting.value = false
        currentTurn.value = if (currentTurn.value == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "board" to boardToFlatMap(board.value),
                        "currentTurn" to currentTurn.value.toString(),
                        "moveHistory" to moveHistory.toList(),
                        "fiftyMoveCounter" to fiftyMoveCounter
                    )
                )
        }
        startTimer()
    }

    fun requestDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(mapOf("drawRequest" to auth.currentUser?.uid))
        }
    }

    fun acceptDraw() {
        endMatch(status = "draw", winner = null)
    }

    fun declineDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(mapOf("drawRequest" to null))
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
                                        transaction.update(player1Ref, mapOf("score" to player1Score + 10))
                                        transaction.update(player2Ref, mapOf("score" to (player2Score - 5).coerceAtLeast(0)))
                                    } else if (winner == player2Id) {
                                        transaction.update(player2Ref, mapOf("score" to player2Score + 10))
                                        transaction.update(player1Ref, mapOf("score" to (player1Score - 5).coerceAtLeast(0)))
                                    } else {
                                        // Trường hợp winner không hợp lệ (null hoặc không khớp với player1Id/player2Id)
                                        // Không làm gì, giữ nguyên điểm số
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

    private fun createInitialBoard(): Array<Array<ChessPiece?>> {
        val newBoard = Array(8) { Array<ChessPiece?>(8) { null } }
        newBoard[0][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 0))
        newBoard[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 1))
        newBoard[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 2))
        newBoard[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE, Position(0, 3))
        newBoard[0][4] = ChessPiece(PieceType.KING, PieceColor.WHITE, Position(0, 4))
        newBoard[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 5))
        newBoard[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 6))
        newBoard[0][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 7))
        for (col in 0..7) {
            newBoard[1][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE, Position(1, col))
        }
        newBoard[7][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 0))
        newBoard[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 1))
        newBoard[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 2))
        newBoard[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK, Position(7, 3))
        newBoard[7][4] = ChessPiece(PieceType.KING, PieceColor.BLACK, Position(7, 4))
        newBoard[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 5))
        newBoard[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 6))
        newBoard[7][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 7))
        for (col in 0..7) {
            newBoard[6][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK, Position(6, col))
        }
        return newBoard
    }

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

    private fun getPossibleMoves(piece: ChessPiece, position: Position): List<Move> {
        val moves = mutableListOf<Move>()
        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) 1 else -1
                val startRow = if (piece.color == PieceColor.WHITE) 1 else 6
                val newRow = position.row + direction
                if (newRow in 0..7 && board.value[newRow][position.col] == null) {
                    moves.add(Move(from = position, position = Position(newRow, position.col), captures = false))
                    if (position.row == startRow && board.value[newRow + direction][position.col] == null) {
                        moves.add(Move(from = position, position = Position(newRow + direction, position.col), captures = false))
                    }
                }
                val captureCols = listOf(position.col - 1, position.col + 1)
                for (col in captureCols) {
                    if (col in 0..7 && newRow in 0..7) {
                        val targetPiece = board.value[newRow][col]
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(Move(from = position, position = Position(newRow, col), captures = true))
                        }
                    }
                }
            }
            else -> {}
        }
        return moves
    }

    private fun positionToString(position: Position): String {
        val col = ('a' + position.col).toString()
        val row = (8 - position.row).toString()
        return "$col$row"
    }

    private fun saveBoardState() {
        val state = getBoardStateHash()
        positionHistory[state] = positionHistory.getOrDefault(state, 0) + 1
    }

    private fun getBoardStateHash(): String {
        val sb = StringBuilder()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                sb.append(
                    if (piece == null) "0"
                    else "${piece.color}_${piece.type}_${piece.position.row}_${piece.position.col}"
                )
            }
        }
        sb.append("|").append(currentTurn.value)
        sb.append("|")
        lastMove?.let {
            sb.append("${it.first.row},${it.first.col}-${it.second.row},${it.second.col}")
        } ?: sb.append("none")
        return sb.toString()
    }

    private fun updateKingPositions() {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                if (piece?.type == PieceType.KING) {
                    if (piece.color == PieceColor.WHITE) {
                        whiteKingPosition = piece.position
                    } else {
                        blackKingPosition = piece.position
                    }
                }
            }
        }
    }

    private fun isKingInCheck(color: PieceColor): Boolean {
        val kingPos = if (color == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = getPossibleMoves(piece, piece.position)
                    if (moves.any { it.position == kingPos }) return true
                }
            }
        }
        return false
    }

    private fun isCheckmate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                if (piece != null && piece.color == currentTurn.value) {
                    val moves = getPossibleMoves(piece, piece.position)
                    for (move in moves) {
                        val from = piece.position
                        val to = move.position
                        val originalPiece = board.value[from.row][from.col]
                        val targetPiece = board.value[to.row][to.col]

                        board.value[to.row][to.col] = piece.copy(position = to)
                        board.value[from.row][from.col] = null

                        val inCheck = isKingInCheck(piece.color)

                        board.value[from.row][from.col] = originalPiece
                        board.value[to.row][to.col] = targetPiece

                        if (!inCheck) return false
                    }
                }
            }
        }
        return true
    }

    private fun isStalemate(): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                if (piece != null && piece.color == currentTurn.value) {
                    val moves = getPossibleMoves(piece, piece.position)
                    for (move in moves) {
                        val from = piece.position
                        val to = move.position
                        val originalPiece = board.value[from.row][from.col]
                        val targetPiece = board.value[to.row][to.col]

                        board.value[to.row][to.col] = piece.copy(position = to)
                        board.value[from.row][from.col] = null

                        val inCheck = isKingInCheck(piece.color)

                        board.value[from.row][from.col] = originalPiece
                        board.value[to.row][to.col] = targetPiece

                        if (!inCheck) return false
                    }
                }
            }
        }
        return true
    }

    private fun isLightSquare(row: Int, col: Int): Boolean {
        return (row + col) % 2 == 1
    }

    private fun checkGameState() {
        if (isGameOver.value) return

        // Kiểm tra không đủ lực chiếu hết
        val pieces = mutableListOf<ChessPiece>()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board.value[row][col]
                if (piece != null) pieces.add(piece)
            }
        }

        val pieceCount = pieces.size
        if (pieceCount == 2) {
            if (pieces.all { it.type == PieceType.KING }) {
                endMatch(status = "draw", winner = null)
                gameResult.value = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua vs Vua)."
                return
            }
        } else if (pieceCount == 3) {
            val kings = pieces.filter { it.type == PieceType.KING }
            val otherPiece = pieces.firstOrNull { it.type != PieceType.KING }
            if (kings.size == 2 && otherPiece != null) {
                if (otherPiece.type == PieceType.BISHOP || otherPiece.type == PieceType.KNIGHT) {
                    endMatch(status = "draw", winner = null)
                    gameResult.value = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua và ${if (otherPiece.type == PieceType.BISHOP) "Tượng" else "Mã"} vs Vua)."
                    return
                }
            }
        } else if (pieceCount == 4) {
            val kings = pieces.filter { it.type == PieceType.KING }
            val bishops = pieces.filter { it.type == PieceType.BISHOP }
            if (kings.size == 2 && bishops.size == 2) {
                val bishop1 = bishops[0]
                val bishop2 = bishops[1]
                val bishop1IsLight = isLightSquare(bishop1.position.row, bishop1.position.col)
                val bishop2IsLight = isLightSquare(bishop2.position.row, bishop2.position.col)
                if (bishop1IsLight == bishop2IsLight) {
                    endMatch(status = "draw", winner = null)
                    gameResult.value = "Hết cờ! Ván đấu hòa (không đủ lực chiếu hết: Vua và Tượng vs Vua và Tượng cùng màu ô)."
                    return
                }
            }
        }

        // Kiểm tra lặp lại vị trí 3 lần
        val currentState = getBoardStateHash()
        if (positionHistory[currentState] ?: 0 >= 3) {
            endMatch(status = "draw", winner = null)
            gameResult.value = "Hết cờ! Ván đấu hòa (lặp lại vị trí 3 lần)."
            return
        }

        // Kiểm tra luật 50 nước
        if (fiftyMoveCounter >= 50) {
            endMatch(status = "draw", winner = null)
            gameResult.value = "Hết cờ! Ván đấu hòa (luật 50 nước: không có pawn move hoặc capture trong 50 nước đi)."
            return
        }

        // Kiểm tra chiếu hết và thế cờ chết
        if (isKingInCheck(currentTurn.value)) {
            if (isCheckmate()) {
                val winner = if (currentTurn.value == PieceColor.WHITE) {
                    matchData.value?.get("player2") as? String
                } else {
                    matchData.value?.get("player1") as? String
                }
                endMatch(status = "checkmate", winner = winner)
                return
            }
        } else if (isStalemate()) {
            endMatch(status = "draw", winner = null)
            gameResult.value = "Hết cờ! Ván đấu hòa (thế cờ chết)."
            return
        }
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