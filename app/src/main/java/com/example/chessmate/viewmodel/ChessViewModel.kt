package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class ChessViewModel : ViewModel() {
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)
    val isPromoting = mutableStateOf(false)
    val playerColor = mutableStateOf(PieceColor.WHITE) // Thêm playerColor, mặc định là WHITE

    fun onSquareClicked(row: Int, col: Int) {
        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (game.getPendingPromotion() != null) {
                    isPromoting.value = true
                } else if (!isGameOver.value && currentTurn.value == PieceColor.BLACK) {
                    triggerAIMove()
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
        if (!isGameOver.value && currentTurn.value == PieceColor.BLACK) {
            triggerAIMove()
        }
    }

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    private fun triggerAIMove() {
        viewModelScope.launch {
            val bestMove = findBestMove()
            if (bestMove != null) {
                val (from, to) = bestMove
                game.selectPiece(from.row, from.col)
                game.movePiece(to)
                updateGameState()
                if (game.getPendingPromotion() != null) {
                    game.promotePawn(PieceType.QUEEN)
                    updateGameState()
                }
            }
        }
    }

    private fun findBestMove(): Pair<Position, Position>? {
        val allMoves = getAllMoves(PieceColor.BLACK)
        if (allMoves.isEmpty()) return null

        var bestMove: Pair<Position, Position>? = null
        var bestCaptureValue = -1

        for (move in allMoves) {
            val (from, to) = move
            val capturedPiece = game.getPieceAt(to.row, to.col)
            if (capturedPiece != null && capturedPiece.color == PieceColor.WHITE) {
                val captureValue = when (capturedPiece.type) {
                    PieceType.PAWN -> 1
                    PieceType.KNIGHT -> 3
                    PieceType.BISHOP -> 3
                    PieceType.ROOK -> 5
                    PieceType.QUEEN -> 9
                    PieceType.KING -> 100
                }
                if (captureValue > bestCaptureValue) {
                    bestCaptureValue = captureValue
                    bestMove = move
                }
            }
        }

        return bestMove ?: allMoves.random()
    }

    private fun getAllMoves(color: PieceColor): List<Pair<Position, Position>> {
        val allMoves = mutableListOf<Pair<Position, Position>>()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = game.getPieceAt(row, col)
                if (piece != null && piece.color == color) {
                    val moves = game.selectPiece(row, col)
                    moves.forEach { move ->
                        allMoves.add(Pair(Position(row, col), move.position))
                    }
                }
            }
        }
        return allMoves
    }

    fun getPendingPromotion(): Position? = game.getPendingPromotion()
}

class FriendChessViewModel : ViewModel() {
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)
    val whiteTime = mutableStateOf(600)
    val blackTime = mutableStateOf(600)
    val isPromoting = mutableStateOf(false)
    val playerColor = mutableStateOf(PieceColor.WHITE) // Thêm playerColor, mặc định là WHITE
    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!isGameOver.value && !isPromoting.value) {
                delay(1000L)
                if (currentTurn.value == PieceColor.WHITE) {
                    whiteTime.value = (whiteTime.value - 1).coerceAtLeast(0)
                    if (whiteTime.value == 0) {
                        isGameOver.value = true
                        gameResult.value = "Black wins by timeout!"
                        break
                    }
                } else {
                    blackTime.value = (blackTime.value - 1).coerceAtLeast(0)
                    if (blackTime.value == 0) {
                        isGameOver.value = true
                        gameResult.value = "White wins by timeout!"
                        break
                    }
                }
            }
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value) return

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

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    fun getPendingPromotion(): Position? = game.getPendingPromotion()

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}