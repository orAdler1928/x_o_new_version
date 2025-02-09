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
    private int currentGameId;

    public void initialize() throws IOException {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        HelloApplication.setConnection(socket, out, in);

        new Thread(this::listenToServer).start();
        listGames(); // Request game list on initialization
    }

    @FXML
    private void createGame() {
        System.out.println("Creating game...");
        out.println("CREATE");
        Platform.runLater(() -> {
            try {
                HelloApplication.switchToGame(currentGameId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void joinGame() {
        String selectedGame = gameListView.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            currentGameId = Integer.parseInt(selectedGame.split(" ")[1]);
            System.out.println("Joining game: " + currentGameId);
            out.println("JOIN " + currentGameId);
            Platform.runLater(() -> {
                try {
                    HelloApplication.switchToGame(currentGameId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void listGames() {
        System.out.println("Listing games...");
        out.println("LIST");
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message;
                System.out.println("Received from server: " + finalMessage);
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}