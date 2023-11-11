package com.github.luriel0228.pxinviteticket.command;

import com.github.luriel0228.pxinviteticket.PXInviteTicket;
import com.github.luriel0228.pxinviteticket.message.Message;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.message.MessageKey;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import com.github.luriel0228.pxinviteticket.valid.PermissionValid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class InviteTicketCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final InvitedValid invitedValid;
    private FileConfiguration config;
    private final Message msgData = Message.getInstance();

    public InviteTicketCommand(PXInviteTicket plugin, InvitedValid invitedValid) {
        this.plugin = plugin;
        this.invitedValid = invitedValid;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(msgData.getMessage(MessageKey.PLAYER_ONLY));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(msgData.getMessage(MessageKey.WRONG_COMMAND));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "리로드":
                    handleReloadCommand(player);
                    break;
                case "등록":
                    handleRegisterCommand(player, args);
                    break;
                case "목록":
                    handleListCommand(player);
                    break;
                default:
                    player.sendMessage(msgData.getMessage(MessageKey.WRONG_COMMAND));
                    break;
            }
        } catch (SQLException e) {
            player.sendMessage(msgData.getMessage(MessageKey.SQL_ERROR));
            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred.", e);
        }

        return true;
    }

    private void handleReloadCommand(Player player) throws SQLException {
        if (PermissionValid.hasPermission(player, "reload")) {
            plugin.reloadConfig();
            MessageConfig.reload();
            player.sendMessage(msgData.getMessage(MessageKey.RELOAD_CONFIG));
        }
    }

    private void handleRegisterCommand(Player player, String @NotNull [] args) throws SQLException {
        config = plugin.getConfig();

        if (args.length > 1) {
            String invitedPlayerName = args[1];

            if (invitedPlayerName.isEmpty()) {
                player.sendMessage(msgData.getMessage(MessageKey.MISSING_PLAYER));
                return;
            }

            int inviteLimit = config.getInt("InviteTicket.InviteLimit");
            int playerinvitedcount = invitedValid.getInvitesCount(player.getName());
            if (playerinvitedcount >= inviteLimit) {
                player.sendMessage(msgData.getMessage(MessageKey.MAX_INVITES_REACHED));
                return;
            }

            if (invitedValid.hasReceivedInvite(invitedPlayerName)) {
                String message = msgData.getMessage(MessageKey.INVITED_PLAYER);
                String formattedMessage = message.replace("{player}", invitedPlayerName);
                player.sendMessage(formattedMessage);
                return;
            }

            invitedValid.registerInvite(player.getName(), invitedPlayerName);
            String message = msgData.getMessage(MessageKey.SET_INVITE_SUCCESS);
            String formattedMessage = message.replace("{player}", invitedPlayerName);
            player.sendMessage(formattedMessage);
        } else {
            player.sendMessage(msgData.getMessage(MessageKey.MISSING_PLAYER));
        }
    }

    private void handleListCommand(@NotNull Player player) throws SQLException {
        this.config = plugin.getConfig();
        List<String> invitedUsers = getInvitedUsers(player.getName());
        int inviteLimit = config.getInt("InviteTicket.InviteLimit");
        if (invitedUsers.isEmpty()) {
            player.sendMessage(msgData.getMessage(MessageKey.NO_INVITED_PLAYERS));
            return;
        }
        String message = msgData.getMessage(MessageKey.INVITED_PLAYER);
        int inviteCount = invitedValid.getInvitesCount(player.getName());
        String formattedMessage = message.replace("{int}", String.valueOf(inviteCount))
                .replace("{max}", String.valueOf(inviteLimit));
        player.sendMessage(formattedMessage);
        invitedUsers.forEach(invitedUser -> player.sendMessage("- " + invitedUser));
    }

    private @NotNull List<String> getInvitedUsers(String inviterName){
        List<String> invitedUsers = new ArrayList<>();
        for (Map.Entry<String, String> entry : invitedValid.getInvites().entrySet()) {
            if (entry.getValue().equals(inviterName)) {
                invitedUsers.add(entry.getKey());
            }
        }
        return invitedUsers;
    }
}