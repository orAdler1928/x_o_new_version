package com.example.x_o_new_version;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.Socket;

public class TicTacToeController {
    @FXML
    private GridPane gridPane;
    @FXML
    private Label messageLabel;
    @FXML
    private Label playerXLabel;
    @FXML
    private Label playerOLabel;
    @FXML
    private Label turnLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label playerIdLabel;
    @FXML
    private Label gameIdLabel;
    @FXML
    private Label connectedPlayersLabel;

    private Button[] buttons = new Button[9];
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private char player;
    private boolean gameOver = false;

    public void initialize() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        for (int i = 0; i < 9; i++) {
            buttons[i] = new Button(" ");
            buttons[i].setMinSize(100, 100);
            buttons[i].getStyleClass().add("tic-tac-toe-button");
            final int index = i;
            buttons[i].setOnAction(e -> makeMove(index));
            gridPane.add(buttons[i], i % 3, i / 3);
        }

        new Thread(this::listenToServer).start();
    }

    private void makeMove(int index) {
        if (!gameOver && buttons[index].getText().equals(" ")) {
            out.println("MOVE " + index);
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message; // Declare as final
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showMessage("Connection lost."));
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("WELCOME")) {
            player = message.charAt(8);
            if (player == 'X') {
                playerXLabel.setText("Player X: You");
                playerOLabel.setText("Player O: Opponent");
            } else {
                playerXLabel.setText("Player X: Opponent");
                playerOLabel.setText("Player O: You");
            }
        } else if (message.startsWith("[")) {
            updateBoard(message);
        } else if (message.equals("INVALID MOVE")) {
            showMessage("Invalid move, try again.");
        } else if (message.startsWith("PLAYER")) {
            showMessage(message);
            gameOver = true;
        } else if (message.equals("DRAW")) {
            showMessage("It's a draw!");
            gameOver = true;
        } else if (message.startsWith("TURN")) {
            showTurn(message.charAt(5));
        } else if (message.equals("WAITING FOR SECOND PLAYER")) {
            showStatus("Waiting for second player...");
        } else if (message.equals("SECOND PLAYER JOINED")) {
            showStatus("Second player joined. Game starting...");
        } else if (message.startsWith("PLAYER ID")) {
            playerIdLabel.setText("Player ID: " + message.split(" ")[2]);
        } else if (message.startsWith("GAME ID")) {
            gameIdLabel.setText("Game ID: " + message.split(" ")[2]);
        } else if (message.startsWith("CONNECTED PLAYERS")) {
            connectedPlayersLabel.setText("Connected Players: " + message.split(" ")[2]);
        }
    }

    private void updateBoard(String boardString) {
        Platform.runLater(() -> {
            String[] boardArray = boardString.replaceAll("[\\[\\] ]", "").split(",");
            for (int i = 0; i < boardArray.length; i++) {
                buttons[i].setText(boardArray[i]);
                buttons[i].getStyleClass().removeAll("x-button", "o-button");
                if (boardArray[i].equals("X")) {
                    buttons[i].getStyleClass().add("x-button");
                } else if (boardArray[i].equals("O")) {
                    buttons[i].getStyleClass().add("o-button");
                }
            }
        });
    }

    private void showMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    private void showTurn(char currentPlayer) {
        Platform.runLater(() -> turnLabel.setText("Turn: Player " + currentPlayer));
    }

    private void showStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }
}