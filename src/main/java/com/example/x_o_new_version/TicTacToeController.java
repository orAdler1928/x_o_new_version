package com.example.x_o_new_version;

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

    private Button[] buttons = new Button[9];
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private char player;
    private boolean gameOver = false;

    public void initialize() throws IOException {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
        if (!gameOver) {
            out.println("MOVE " + index);
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message; // Make message effectively final
                if (message.startsWith("WELCOME")) {
                    player = message.charAt(8);
                    javafx.application.Platform.runLater(() -> {
                        if (player == 'X') {
                            playerXLabel.setText("Player X: You");
                            playerOLabel.setText("Player O: Opponent");
                        } else {
                            playerXLabel.setText("Player X: Opponent");
                            playerOLabel.setText("Player O: You");
                        }
                    });
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBoard(String boardString) {
        javafx.application.Platform.runLater(() -> {
            String[] boardArray = boardString.replaceAll("[\\[\\] ]", "").split(",");
            for (int i = 0; i < boardArray.length; i++) {
                buttons[i].setText(boardArray[i]);
                if (boardArray[i].equals("X")) {
                    buttons[i].getStyleClass().add("x-button");
                } else if (boardArray[i].equals("O")) {
                    buttons[i].getStyleClass().add("o-button");
                }
            }
        });
    }

    private void showMessage(String message) {
        javafx.application.Platform.runLater(() -> messageLabel.setText(message));
    }

    private void showTurn(char currentPlayer) {
        javafx.application.Platform.runLater(() -> turnLabel.setText("Turn: Player " + currentPlayer));
    }
}