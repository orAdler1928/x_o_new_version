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
        waitingClients.add(client);
        client.setCurrentGame(game);
        client.sendMessage("GAME CREATED " + (games.size() - 1));
        client.sendMessage("WAITING FOR SECOND PLAYER");
        game.updatePlayerStatus();
        game.sendGameId();
        game.sendPlayerIds();
        broadcastGameList();
    }

    public static synchronized void joinGame(ClientHandler client, int gameId) {
        if (gameId < games.size() && games.get(gameId).addPlayer(client)) {
            waitingClients.remove(games.get(gameId).getPlayer1());
            client.setCurrentGame(games.get(gameId));
            games.get(gameId).startGame();
            games.get(gameId).updatePlayerStatus();
            games.get(gameId).sendGameId();
            games.get(gameId).sendPlayerIds();
            broadcastGameList();
            client.sendMessage("GAME JOINED");
        } else {
            client.sendMessage("INVALID GAME ID");
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

    public static synchronized void makeMove(ClientHandler client, int index) {
        Game currentGame = client.getCurrentGame();
        if (currentGame != null) {
            currentGame.makeMove(index, client.getPlayer());
        } else {
            client.sendMessage("NO GAME ASSIGNED");
        }
    }
}