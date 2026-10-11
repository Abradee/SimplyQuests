package io.github.abradee.simplyQuests.Managers;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(File dataFolder, String fileName) throws SQLException {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, fileName);
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        createTables();
    }

    private void createTables() throws SQLException {
        String playersSql = """
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                points INTEGER NOT NULL DEFAULT 0
            );
            """;

        String questsSql = """
            CREATE TABLE IF NOT EXISTS player_quests (
                uuid TEXT,
                quest_id TEXT,
                progress INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (uuid, quest_id)
            );
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(playersSql);
            statement.execute(questsSql);
        }
    }

    // Loads a player's quest progress from the database
    public Map<String, Integer> loadPlayerQuests(UUID uuid) {
        Map<String, Integer> progressMap = new HashMap<>();
        String sql = "SELECT quest_id, progress FROM player_quests WHERE uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    progressMap.put(rs.getString("quest_id"), rs.getInt("progress"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return progressMap;
    }

    // Saves or updates a specific quest's progress for a player
    public void saveQuestProgress(UUID uuid, String questId, int progress) {
        String sql = "INSERT OR REPLACE INTO player_quests (uuid, quest_id, progress) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, questId);
            ps.setInt(3, progress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}