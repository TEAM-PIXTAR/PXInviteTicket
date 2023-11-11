package com.github.luriel0228.pxinviteticket.file;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFile {

    private static final Logger logger = Logger.getLogger(DataFile.class.getName());

    private Connection connection;

    public DataFile(String dbName) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            createTables();
        } catch (SQLException e) {
            handleSQLException("연결 또는 테이블 생성 중 오류가 발생했습니다", e);
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
            handleSQLException("테이블 생성 중 오류가 발생했습니다", e);
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
            handleSQLException("연결을 닫는 중 오류가 발생했습니다", e);
        }
    }

    private void handleSQLException(String errorMessage, SQLException e) {
        logger.log(Level.SEVERE, errorMessage, e);
    }
}