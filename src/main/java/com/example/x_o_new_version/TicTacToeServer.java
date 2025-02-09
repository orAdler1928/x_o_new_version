package com.example.x_o_new_version;

import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private static final int PORT = 12345;
    private static List<Game> games = Collections.synchronizedList(new ArrayList<>());
    private static List<ClientHandler> waitingClients = Collections.synchronizedList(new ArrayList<>());

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

public static synchronized void createGame(ClientHandler client) {
    Game game = new Game(client);
    games.add(game);
    client.setCurrentGame(game);
    client.sendMessage("GAME CREATED " + (games.size() - 1));
    client.setPlayer('X'); // This will send the WELCOME message
    client.sendMessage("WAITING FOR SECOND PLAYER");
    game.updatePlayerStatus();
    game.sendGameId();
    game.sendPlayerIds();
    broadcastGameList();
    System.out.println("Game created: " + (games.size() - 1));
}

public static synchronized void joinGame(ClientHandler client, int gameId) {
    System.out.println("Attempting to join game: " + gameId);
    if (gameId >= 0 && gameId < games.size() && games.get(gameId).addPlayer(client)) {
        client.setCurrentGame(games.get(gameId));
        client.sendMessage("GAME JOINED");
        client.setPlayer('O'); // This will send the WELCOME message
        games.get(gameId).startGame();
        games.get(gameId).updatePlayerStatus();
        games.get(gameId).sendGameId();
        games.get(gameId).sendPlayerIds();
        broadcastGameList();
        System.out.println("Game joined: " + gameId);
    } else {
        client.sendMessage("INVALID GAME ID");
        System.out.println("Invalid game ID: " + gameId);
    }
}

    public static synchronized void broadcastGameList() {
        List<String> availableGames = getAvailableGames();
        for (ClientHandler client : waitingClients) {
            client.sendMessage("GAMES " + String.join(",", availableGames));
        }
    }

    public static synchronized List<String> getAvailableGames() {
        List<String> availableGames = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            availableGames.add("Game " + i);
        }
        return availableGames;
    }

    public static synchronized void makeMove(ClientHandler client, int index, int gameId) {
        System.out.println("Making move: " + index + " by player: " + client.getPlayer());
        Game currentGame = games.get(gameId);
        if (currentGame != null && currentGame.isPlayerTurn(client)) {
            currentGame.makeMove(index, client.getPlayer());
            currentGame.broadcastBoard(); // Send the updated board to both players
            currentGame.broadcastTurn(); // Send the current turn to both players
            if (currentGame.checkWin(client.getPlayer())) {
                currentGame.broadcastMessage("PLAYER " + client.getPlayer() + " WINS");
                currentGame.setGameOver(true);
            } else if (currentGame.checkDraw()) {
                currentGame.broadcastMessage("DRAW");
                currentGame.setGameOver(true);
            }
        } else {
            client.sendMessage("INVALID MOVE");
            System.out.println("Invalid move by player: " + client.getPlayer());
        }
    }
}