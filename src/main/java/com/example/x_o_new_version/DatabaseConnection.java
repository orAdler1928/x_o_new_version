package com.example.x_o_new_version;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:game_times.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS game_times (\n"
                + " id integer PRIMARY KEY,\n"
                + " game_id integer NOT NULL,\n"
                + " time_seconds integer NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<String> getGameDurations() {
        String sql = "SELECT game_id, MAX(time_seconds) as duration FROM game_times GROUP BY game_id ORDER BY duration DESC";
        List<String> durations = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int gameId = rs.getInt("game_id");
                int duration = rs.getInt("duration");
                durations.add("Game ID: " + gameId + ", Duration: " + duration + " seconds");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return durations;
    }
}