package com.example.x_o_new_version;

import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private static final int PORT = 12345;
    private static Game game = new Game();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());
            ClientHandler clientHandler = new ClientHandler(socket);
            new Thread(clientHandler).start();
        }
    }

    public static synchronized void addPlayer(ClientHandler client) {
        if (game.addPlayer(client)) {
            client.sendMessage("WELCOME " + client.getPlayer());
            if (game.isReady()) {
                game.startGame();
            }
        } else {
            client.sendMessage("GAME FULL");
        }
    }

    public static synchronized void makeMove(ClientHandler client, int index) {
        game.makeMove(index, client.getPlayer());
    }
}

class Game {
    private ClientHandler player1;
    private ClientHandler player2;
    private char[] board = new char[9];
    private char currentPlayer = 'X';
    private boolean gameOver = false;

    public Game() {
        Arrays.fill(board, ' ');
    }

    public boolean addPlayer(ClientHandler player) {
        if (player1 == null) {
            player1 = player;
            player.setPlayer('X');
            return true;
        } else if (player2 == null) {
            player2 = player;
            player.setPlayer('O');
            return true;
        }
        return false;
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
        }
    }

    private void broadcastBoard() {
        String boardState = Arrays.toString(board);
        player1.sendBoard(boardState);
        player2.sendBoard(boardState);
    }

    private void broadcastMessage(String message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
    }

    private void broadcastTurn() {
        player1.sendMessage("TURN " + currentPlayer);
        player2.sendMessage("TURN " + currentPlayer);
    }

    private boolean checkWin(char player) {
        int[][] winPositions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
                {0, 4, 8}, {2, 4, 6}             // diagonals
        };

        for (int[] pos : winPositions) {
            if (board[pos[0]] == player && board[pos[1]] == player && board[pos[2]] == player) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDraw() {
        for (char c : board) {
            if (c == ' ') {
                return false;
            }
        }
        return true;
    }
}