package com.example.x_o_new_version;

        import java.io.*;
        import java.net.*;
        import java.util.List;

        public class ClientHandler implements Runnable {
            private Socket socket;
            private PrintWriter out;
            private BufferedReader in;
            private char player;
            private Game currentGame;

            public ClientHandler(Socket socket) throws IOException {
                this.socket = socket;
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }

            @Override
            public void run() {
                try {
                    String input;
                    while ((input = in.readLine()) != null) {
                        System.out.println("Received command: " + input);
                        if (input.startsWith("CREATE")) {
                            TicTacToeServer.createGame(this);
                        } else if (input.startsWith("JOIN")) {
                            int gameId = Integer.parseInt(input.split(" ")[1]);
                            TicTacToeServer.joinGame(this, gameId);
                        } else if (input.startsWith("MOVE")) {
                            String[] parts = input.split(" ");
                            int index = Integer.parseInt(parts[1]);
                            int gameId = Integer.parseInt(parts[2]);
                            TicTacToeServer.makeMove(this, index, gameId);
                        } else if (input.startsWith("LIST")) {
                            List<String> availableGames = TicTacToeServer.getAvailableGames();
                            sendMessage("GAMES " + String.join(",", availableGames));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void sendBoard(String board) {
                out.println(board);
                System.out.println("Sent board: " + board);
            }

            public void sendMessage(String message) {
                out.println(message);
                System.out.println("Sent message: " + message);
            }

            public void setPlayer(char player) {
                this.player = player;
                System.out.println("Player set to: " + player);
                sendMessage("WELCOME " + player);
            }

            public char getPlayer() {
                return player;
            }

            public void setCurrentGame(Game game) {
                this.currentGame = game;
            }

            public Game getCurrentGame() {
                return currentGame;
            }
        }