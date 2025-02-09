package com.example.x_o_new_version;

import java.util.Arrays;

class Game {
    private ClientHandler player1;
    private ClientHandler player2;
    private char[] board = new char[9];
    private char currentPlayer = 'X';
    private boolean gameOver = false;

    public Game(ClientHandler player1) {
        this.player1 = player1;
        Arrays.fill(board, ' ');
    }

    public boolean addPlayer(ClientHandler player) {
        if (player2 == null && player != player1) {
            player2 = player;
            player.setPlayer('O');
            player1.sendMessage("SECOND PLAYER JOINED");
            player2.sendMessage("SECOND PLAYER JOINED");
            updatePlayerStatus();
            sendGameId();
            sendPlayerIds();
            System.out.println("Second player joined game: " + this.hashCode());
            return true;
        }
        return false;
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public boolean isReady() {
        return player1 != null && player2 != null;
    }

    public void startGame() {
        broadcastBoard();
        broadcastTurn();
    }

    public synchronized void makeMove(int index, char player) {
        if (!gameOver && board[index] == ' ' && player == currentPlayer) {
            board[index] = player;
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            broadcastBoard();
            broadcastTurn();
            if (checkWin(player)) {
                broadcastMessage("PLAYER " + player + " WINS");
                gameOver = true;
            } else if (checkDraw()) {
                broadcastMessage("DRAW");
                gameOver = true;
            }
            System.out.println("Move made: " + index + " by player: " + player);
        }
    }

    public boolean isPlayerTurn(ClientHandler player) {
        return (player == player1 && currentPlayer == 'X') || (player == player2 && currentPlayer == 'O');
    }

    public void broadcastBoard() {
        String boardState = Arrays.toString(board);
        player1.sendBoard(boardState);
        if (player2 != null) {
            player2.sendBoard(boardState);
        }
        System.out.println("Broadcast board: " + boardState);
    }

    public void broadcastMessage(String message) {
        player1.sendMessage(message);
        if (player2 != null) {
            player2.sendMessage(message);
        }
        System.out.println("Broadcast message: " + message);
    }

    public void broadcastTurn() {
        player1.sendMessage("TURN " + currentPlayer);
        if (player2 != null) {
            player2.sendMessage("TURN " + currentPlayer);
        }
    }

    public boolean checkWin(char player) {
        int[][] winPositions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] pos : winPositions) {
            if (board[pos[0]] == player && board[pos[1]] == player && board[pos[2]] == player) {
                return true;
            }
        }
        return false;
    }

    public boolean checkDraw() {
        for (char c : board) {
            if (c == ' ') {
                return false;
            }
        }
        return true;
    }

    public void updatePlayerStatus() {
        String status = "CONNECTED PLAYERS " + (player2 == null ? 1 : 2);
        player1.sendMessage(status);
        if (player2 != null) {
            player2.sendMessage(status);
        }
    }

    public void sendGameId() {
        String gameIdMessage = "GAME ID " + this.hashCode();
        player1.sendMessage(gameIdMessage);
        if (player2 != null) {
            player2.sendMessage(gameIdMessage);
        }
    }

    public void sendPlayerIds() {
        player1.sendMessage("PLAYER ID " + player1.hashCode());
        if (player2 != null) {
            player2.sendMessage("PLAYER ID " + player2.hashCode());
        }
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}