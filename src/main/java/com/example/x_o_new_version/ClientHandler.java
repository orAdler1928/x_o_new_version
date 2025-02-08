package com.example.x_o_new_version;

            import java.io.*;
            import java.net.*;

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
                        TicTacToeServer.addPlayer(this);
                        String input;
                        while ((input = in.readLine()) != null) {
                            System.out.println("Received command: " + input);
                            if (input.startsWith("MOVE")) {
                                int index = Integer.parseInt(input.split(" ")[1]);
                                TicTacToeServer.makeMove(this, index);
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
                }

                public void sendMessage(String message) {
                    out.println(message);
                }

                public void setPlayer(char player) {
                    this.player = player;
                }

                public char getPlayer() {
                    return player;
                }
            }