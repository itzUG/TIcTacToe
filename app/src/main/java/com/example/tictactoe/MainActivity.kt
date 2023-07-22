package com.example.tictactoe

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player1Score = 0
    private var player2Score = 0
    private var currentPlayer = 1
    private val board: Array<Int> = Array(9) { 0 }
    private var isGameOver = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("TicTacToeGame", Context.MODE_PRIVATE)

        // Set click listeners for the image views
        val imageViews = listOf(
            binding.image1, binding.image2, binding.image3,
            binding.image4, binding.image5, binding.image6,
            binding.image7, binding.image8, binding.image9
        )
        for (i in 0..8) {
            imageViews[i].setOnClickListener { onCellClick(i, imageViews[i]) }
        }

        // Set click listener for reset button
        binding.resetbutton.setOnClickListener {
            resetGame()
        }

        // Set click listener for save game button
        binding.savegame.setOnClickListener {
            saveGame()
        }

        // Load the saved game state
        loadGame()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCellClick(index: Int, imageView: ImageView) {
        if (board[index] == 0 && !isGameOver) {
            // Check if the cell is not already filled and the game is not over
            board[index] = currentPlayer

            if (currentPlayer == 1) {
                // Set drawable image for Player 1
                imageView.setImageResource(R.drawable.x)
                currentPlayer = 2
            } else {
                // Set drawable image for Player 2
                imageView.setImageResource(R.drawable.o)
                currentPlayer = 1
            }

            // Check for a win or draw
            if (checkWin() || checkDraw()) {
                disableAllCells()
                if (checkWin()) {
                    // Increment score for the winner
                    if (currentPlayer == 1) player2Score++ else player1Score++
                }
                showWinnerOrDraw()

                // Save the game state when the game is over
                saveGame()
            }
        } else if (!isGameOver) {
            // Vibrate and show toast for wrong move
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            Toast.makeText(this, "Wrong Move!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableAllCells() {
        // Disable all cells when the game is over
        for (i in 0..8) {
            val imageView = getCellImageView(i)
            imageView.isEnabled = false
        }
    }

    private fun getCellImageView(index: Int): ImageView {
        // Helper method to get the ImageView for a given cell index
        val imageViews = listOf(
            binding.image1, binding.image2, binding.image3,
            binding.image4, binding.image5, binding.image6,
            binding.image7, binding.image8, binding.image9
        )
        return imageViews[index]
    }

    private fun checkWin(): Boolean {
        // Implement the logic to check for a win (three in a row/column/diagonal)
        // You can use the board array to check the state of the game

        // Check rows
        for (i in 0..6 step 3) {
            if (board[i] != 0 && board[i] == board[i + 1] && board[i] == board[i + 2]) {
                return true
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[i] != 0 && board[i] == board[i + 3] && board[i] == board[i + 6]) {
                return true
            }
        }

        // Check diagonals
        if (board[0] != 0 && board[0] == board[4] && board[0] == board[8]) {
            return true
        }
        if (board[2] != 0 && board[2] == board[4] && board[2] == board[6]) {
            return true
        }

        return false
    }

    private fun checkDraw(): Boolean {
        // Implement the logic to check for a draw (all cells are filled and no winner)
        // You can use the board array to check the state of the game
        return board.all { it != 0 }
    }

    private fun showWinnerOrDraw() {
        // Show a toast message with the winner or a draw message
        val message = if (checkWin()) {
            val winner = if (currentPlayer == 1) "Player 2" else "Player 1"
            "$winner wins!"
        } else {
            "It's a draw!"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Update the player score text views
        binding.player1.text = "Player 1: $player1Score"
        binding.player2.text = "Player 2: $player2Score"

        // Game is over
        isGameOver = true
    }

    private fun resetGame() {
        // Reset the game board and enable all cells
        for (i in 0..8) {
            board[i] = 0
            val imageView = getCellImageView(i)
            imageView.setImageResource(R.drawable.block)
            imageView.isEnabled = true
        }

        // Reset player scores and update the player score text views
        player1Score = 0
        player2Score = 0
        binding.player1.text = "Player 1: 0"
        binding.player2.text = "Player 2: 0"

        // Reset current player and game over flag
        currentPlayer = 1
        isGameOver = false

        // Save the game state after reset
        saveGame()
    }

    private fun saveGame() {
        // Save the current state of the game (board, player scores, etc.) using SharedPreferences

        val editor = sharedPreferences.edit()
        editor.putString("BOARD_STATE", board.joinToString(","))
        editor.putInt("PLAYER_1_SCORE", player1Score)
        editor.putInt("PLAYER_2_SCORE", player2Score)
        editor.putInt("CURRENT_PLAYER", currentPlayer)
        editor.putBoolean("IS_GAME_OVER", isGameOver)
        editor.apply()
    }

    private fun loadGame() {
        // Load the saved game state from SharedPreferences

        // Load board state
        val boardState = sharedPreferences.getString("BOARD_STATE", null)
        boardState?.let {
            val boardValues = it.split(",").map { value -> value.toInt() }
            boardValues.indices.forEach { index ->
                board[index] = boardValues[index]
                if (board[index] != 0) {
                    val imageView = getCellImageView(index)
                    imageView.setImageResource(
                        if (board[index] == 1) R.drawable.x else R.drawable.o
                    )
                }
            }
        }

        // Load player scores
        player1Score = sharedPreferences.getInt("PLAYER_1_SCORE", 0)
        player2Score = sharedPreferences.getInt("PLAYER_2_SCORE", 0)
        binding.player1.text = "Player 1: $player1Score"
        binding.player2.text = "Player 2: $player2Score"

        // Load current player and game over flag
        currentPlayer = sharedPreferences.getInt("CURRENT_PLAYER", 1)
        isGameOver = sharedPreferences.getBoolean("IS_GAME_OVER", false)
    }

    // You may also want to handle orientation changes and save the game state in onSaveInstanceState
    // and restore it in onRestoreInstanceState. This is just a basic example of the game logic.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
       // outState.putIntArray("BOARD_STATE", board)
        outState.putInt("PLAYER_1_SCORE", player1Score)
        outState.putInt("PLAYER_2_SCORE", player2Score)
        outState.putInt("CURRENT_PLAYER", currentPlayer)
        outState.putBoolean("IS_GAME_OVER", isGameOver)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val boardState = savedInstanceState.getIntArray("BOARD_STATE")
        boardState?.let {
           // it.copyInto(board)
            board.indices.forEach { index ->
                if (board[index] != 0) {
                    val imageView = getCellImageView(index)
                    imageView.setImageResource(
                        if (board[index] == 1) R.drawable.x else R.drawable.o
                    )
                }
            }
        }

        player1Score = savedInstanceState.getInt("PLAYER_1_SCORE", 0)
        player2Score = savedInstanceState.getInt("PLAYER_2_SCORE", 0)
        binding.player1.text = "Player 1: $player1Score"
        binding.player2.text = "Player 2: $player2Score"

        currentPlayer = savedInstanceState.getInt("CURRENT_PLAYER", 1)
        isGameOver = savedInstanceState.getBoolean("IS_GAME_OVER", false)
    }
}
