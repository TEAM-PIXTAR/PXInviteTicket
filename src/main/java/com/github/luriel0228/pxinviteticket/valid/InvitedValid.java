package com.github.luriel0228.pxinviteticket.valid;

import com.github.luriel0228.pxinviteticket.file.DataFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class InvitedValid {

    private final Connection connection;

    public InvitedValid(DataFile sqliteManager) {
        this.connection = sqliteManager.getConnection();
    }

    public void registerInvite(String inviter, String invited) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO invites (inviter, invited) VALUES (?, ?)");
            statement.setString(1, inviter);
            statement.setString(2, invited);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasReceivedInvite(String playerName) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM invites WHERE invited = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, String> getInvites() {
        Map<String, String> invites = new HashMap<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM invites");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String inviter = resultSet.getString("inviter");
                String invited = resultSet.getString("invited");
                invites.put(invited, inviter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invites;
    }
}
