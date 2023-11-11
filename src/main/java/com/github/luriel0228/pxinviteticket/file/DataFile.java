package com.github.luriel0228.pxinviteticket.file;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataFile {

    private Connection connection;

    public DataFile(String dbName) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            createTables();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {

            String invitesTableSQL = "CREATE TABLE IF NOT EXISTS invites (" +
                    "inviter TEXT NOT NULL," +
                    "invited TEXT NOT NULL)";
            statement.execute(invitesTableSQL);

            String inviteCountsTableSQL = "CREATE TABLE IF NOT EXISTS invite_counts (" +
                    "inviter TEXT NOT NULL," +
                    "invited_count INT DEFAULT 0)";
            statement.execute(inviteCountsTableSQL);
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
    private void handleSQLException(SQLException e) {
        e.printStackTrace();
    }
}