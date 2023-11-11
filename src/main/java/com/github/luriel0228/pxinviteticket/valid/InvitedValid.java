package com.github.luriel0228.pxinviteticket.valid;

import com.github.luriel0228.pxinviteticket.file.DataFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

public class InvitedValid {

    private final Connection connection;
    private static final Logger logger = Logger.getLogger(InvitedValid.class.getName());

    private static final String INSERT_INVITE_QUERY = "INSERT INTO invites (inviter, invited) VALUES (?, ?)";
    private static final String UPDATE_INVITE_COUNT_QUERY = "INSERT OR REPLACE INTO invite_counts (inviter, invited_count) VALUES (?, COALESCE((SELECT invited_count FROM invite_counts WHERE inviter = ?), 0) + 1)";
    private static final String SELECT_INVITES_BY_PLAYER_QUERY = "SELECT * FROM invites WHERE invited = ?";
    private static final String SELECT_ALL_INVITES_QUERY = "SELECT * FROM invites";
    private static final String SELECT_INVITE_COUNT_BY_INVITER_QUERY = "SELECT invited_count FROM invite_counts WHERE inviter = ?";

    public InvitedValid(@NotNull DataFile sqliteManager) {
        this.connection = sqliteManager.getConnection();
    }

    public void registerInvite(String inviter, String invited) {
        try {
            executeUpdateQuery(INSERT_INVITE_QUERY, inviter, invited);
            updateInviteCount(inviter);
        } catch (SQLException e) {
            handleSQLException(e, "Error while registering invite", INSERT_INVITE_QUERY, inviter, invited);
        }
    }

    private void updateInviteCount(String inviter) {
        try {
            executeUpdateQuery(UPDATE_INVITE_COUNT_QUERY, inviter, inviter);
        } catch (SQLException e) {
            handleSQLException(e, "Error while updating invite count", UPDATE_INVITE_COUNT_QUERY, inviter, inviter);
        }
    }

    public int getInvitesCount(String inviterName) {
        try {
            return executeSelectSingleIntQuery(SELECT_INVITE_COUNT_BY_INVITER_QUERY, inviterName);
        } catch (SQLException e) {
            handleSQLException(e, "Error while getting invite count", SELECT_INVITE_COUNT_BY_INVITER_QUERY, inviterName);
            return 0;
        }
    }

    public boolean hasReceivedInvite(String playerName) {
        try {
            return executeSelectQuery(SELECT_INVITES_BY_PLAYER_QUERY, playerName).next();
        } catch (SQLException e) {
            handleSQLException(e, "Error while checking received invite", SELECT_INVITES_BY_PLAYER_QUERY, playerName);
            return false;
        }
    }

    public Map<String, String> getInvites() {
        Map<String, String> invites = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_INVITES_QUERY);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String inviter = resultSet.getString("inviter");
                String invited = resultSet.getString("invited");
                invites.put(invited, inviter);
            }
        } catch (SQLException e) {
            handleSQLException(e, "모든 초대를 검색하는 동안 오류가 발생했습니다.", SELECT_ALL_INVITES_QUERY);
        }
        return invites;
    }

    private void executeUpdateQuery(String query, String @NotNull ... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            setPreparedStatementParameters(statement, params);
            statement.executeUpdate();
        }
    }

    private ResultSet executeSelectQuery(String query, String @NotNull ... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            setPreparedStatementParameters(statement, params);
            return statement.executeQuery();
        }
    }

    private int executeSelectSingleIntQuery(String query, String @NotNull ... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            setPreparedStatementParameters(statement, params);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void setPreparedStatementParameters(PreparedStatement statement, String @NotNull ... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
    }

    private void handleSQLException(@NotNull SQLException e, String errorMessage, String query, String... params) {
        logger.log(Level.WARNING, errorMessage + " - Query: " + query + " - Params: " + String.join(", ", params), e);
    }
}
