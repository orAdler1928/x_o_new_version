package com.example.x_o_new_version;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
    @FXML
    private Label timerLabel;
    @FXML
    private ListView<String> leaderboardListView;

    private Button[] buttons = new Button[9];
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private char player;
    private boolean gameOver = false;
    private int gameId;
    private Timeline timeline;
    private int timeSeconds = 0;

    public void initialize() {
        for (int i = 0; i < 9; i++) {
            buttons[i] = new Button("");
            buttons[i].setMinSize(100, 100);
            buttons[i].getStyleClass().add("tic-tac-toe-button");
            final int index = i;
            buttons[i].setOnAction(e -> makeMove(index));
            gridPane.add(buttons[i], i % 3, i / 3);
        }
        // Initialize labels
        playerXLabel.setText("Player X: ");
        playerOLabel.setText("Player O: ");
        turnLabel.setText("Turn: ");
        statusLabel.setText("");
        playerIdLabel.setText("Player ID: ");
        gameIdLabel.setText("Game ID: ");
        connectedPlayersLabel.setText("Connected Players: ");

        // Initialize timeline
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeSeconds++;
            timerLabel.setText("Time: " + timeSeconds);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
        gameIdLabel.setText("Game ID: " + gameId);
    }

    public void setConnection(Socket socket, PrintWriter out, BufferedReader in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        new Thread(this::listenToServer).start();
    }

    private void makeMove(int index) {
        if (!gameOver && buttons[index].getText().equals("")) {
            out.println("MOVE " + index + " " + gameId);
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message;
                System.out.println("Received from server: " + finalMessage);
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showMessage("Connection lost."));
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("Handling server message: " + message);
        if (message.startsWith("WELCOME")) {
            player = message.charAt(8);
            System.out.println("Player set to: " + player);
            if (player == 'X') {
                playerXLabel.setText("Player X: You");
                playerOLabel.setText("Player O: Opponent");
            } else {
                playerXLabel.setText("Player X: Opponent");
                playerOLabel.setText("Player O: You");
            }
        } else if (message.startsWith("INDEX")) {
            setGameId(Integer.parseInt(message.split(" ")[1]));
        } else if (message.startsWith("[")) {
            System.out.println("Updating board with message: " + message);
            updateBoard(message);
        } else if (message.equals("INVALID MOVE")) {
            System.out.println("Invalid move received");
            showMessage("Invalid move, try again.");
        } else if (message.startsWith("PLAYER ID")) {
            System.out.println("Player ID message received: " + message);
            playerIdLabel.setText("Player ID: " + message.split(" ")[2]);
        } else if (message.startsWith("PLAYER")) {
            System.out.println("Player message received: " + message);
            showMessage(message);
            gameOver = true;
            if (timeline != null) {
                timeline.stop();
            }
        } else if (message.equals("DRAW")) {
            System.out.println("Draw message received");
            showMessage("It's a draw!");
            gameOver = true;
            if (timeline != null) {
                timeline.stop();
            }
        } else if (message.startsWith("TURN")) {
            System.out.println("Turn message received: " + message);
            showTurn(message.charAt(5));
        } else if (message.equals("WAITING FOR SECOND PLAYER")) {
            System.out.println("Waiting for second player message received");
            showStatus("Waiting for second player...");
        } else if (message.equals("SECOND PLAYER JOINED")) {
            System.out.println("Second player joined message received");
            showStatus("Second player joined. Game starting...");
        } else if (message.startsWith("GAME ID")) {
            System.out.println("Game ID message received: " + message);
            gameIdLabel.setText("Game ID: " + message.split(" ")[2]);
        } else if (message.startsWith("CONNECTED PLAYERS")) {
            System.out.println("Connected players message received: " + message);
            connectedPlayersLabel.setText("Connected Players: " + message.split(" ")[2]);
        } else if (message.startsWith("TIME")) {
            timeSeconds = Integer.parseInt(message.split(" ")[1]);
            timerLabel.setText("Time: " + timeSeconds);
        } else if (message.startsWith("LEADERBOARD")) {
            Platform.runLater(() -> {
                leaderboardListView.getItems().clear();
                String[] leaderboard = message.substring(12).split(",");
                for (String entry : leaderboard) {
                    if (!entry.trim().isEmpty()) {
                        leaderboardListView.getItems().add(entry);
                    }
                }
            });
        } else {
            System.out.println("Unknown message received: " + message);
        }
    }

    private void printBoard(String[] boardString) {
        System.out.println("Printing board: ");
        for (int i = 0; i < boardString.length; i++) {
            System.out.print(boardString[i] + " ");
            if ((i + 1) % 3 == 0) {
                System.out.println();
            }
        }
    }

    private void updateBoard(String boardString) {
        String[] boardArray = boardString.replaceAll("[\\[\\] ]", "").split(",");
        printBoard(boardArray);
        for (int i = 0; i < boardArray.length; i++) {
            String cell = boardArray[i].trim();
            buttons[i].setText(cell);
            buttons[i].getStyleClass().removeAll("x-button", "o-button");
            if (cell.equals("X")) {
                buttons[i].getStyleClass().add("x-button");
            } else if (cell.equals("O")) {
                buttons[i].getStyleClass().add("o-button");
            }
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }

    private void showTurn(char currentPlayer) {
        turnLabel.setText("Turn: Player " + currentPlayer);
    }

    private void showStatus(String status) {
        statusLabel.setText(status);
    }
}