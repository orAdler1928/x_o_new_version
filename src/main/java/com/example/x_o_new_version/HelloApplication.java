package com.example.x_o_new_version;

    import javafx.application.Application;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Scene;
    import javafx.stage.Stage;

    import java.io.*;
    import java.net.Socket;

    public class HelloApplication extends Application {
        private static Stage primaryStage;
        private static Socket socket;
        private static PrintWriter out;
        private static BufferedReader in;

        @Override
        public void start(Stage stage) throws IOException {
            primaryStage = stage;
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Tic Tac Toe - Login");
            stage.setScene(scene);
            stage.show();
        }

        public static void switchToGame(int gameId) throws IOException {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("tic-tac-toe-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            TicTacToeController controller = fxmlLoader.getController();
            controller.setGameId(gameId);
            controller.setConnection(socket, out, in);
            primaryStage.setTitle("Tic Tac Toe");
            primaryStage.setScene(scene);
        }

        public static void setConnection(Socket socket, PrintWriter out, BufferedReader in) {
            HelloApplication.socket = socket;
            HelloApplication.out = out;
            HelloApplication.in = in;
        }

        public static void main(String[] args) {
            launch();
        }
    }