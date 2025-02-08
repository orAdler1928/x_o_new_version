package com.example.x_o_new_version;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class LoginController {
    @FXML
    private ListView<String> gameListView;
    @FXML
    private Label statusLabel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void initialize() throws IOException {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(this::listenToServer).start();
        listGames(); // Request game list on initialization
    }

    @FXML
    private void createGame() {
        out.println("CREATE");
    }

    @FXML
    private void joinGame() {
        String selectedGame = gameListView.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            int gameId = Integer.parseInt(selectedGame.split(" ")[1]);
            out.println("JOIN " + gameId);
        }
    }

    @FXML
    private void listGames() {
        out.println("LIST");
    }

    private void listenToServer() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    final String finalMessage = message;
                    if (message.startsWith("GAMES")) {
                        Platform.runLater(() -> {
                            gameListView.getItems().clear();
                            String[] games = finalMessage.substring(6).split(",");
                            for (String game : games) {
                                if (!game.trim().isEmpty()) {
                                    gameListView.getItems().add(game);
                                }
                            }
                        });
                    } else if (message.startsWith("GAME CREATED") || message.startsWith("GAME JOINED")) {
                        Platform.runLater(() -> {
                            try {
                                HelloApplication.switchToGame();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (message.equals("WAITING FOR SECOND PLAYER")) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Waiting for second player...");
                        });
                    } else if (message.equals("SECOND PLAYER JOINED")) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Second player joined. Game starting...");
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
