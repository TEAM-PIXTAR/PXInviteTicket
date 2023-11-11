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

import java.io.File;
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

        // 플레이어만 해당 명령어 사용 가능
        if (!(sender instanceof Player)) {
            sender.sendMessage(msgData.getMessage(MessageKey.PLAYER_ONLY));
            return true;
        }

        Player player = (Player) sender;

        // 잘못된 명령어 형식
        if (args.length == 0) {
            player.sendMessage(msgData.getMessage(MessageKey.WRONG_COMMAND));
            return true;
        }

        try {
            // 명령어 처리
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

    private void handleReloadCommand(Player player) {
        // 리로드 권한 확인
        if (PermissionValid.hasPermission(player, "reload")) {
            plugin.reloadConfig();

            File dataFolder = plugin.getDataFolder();

            File configFile = new File(dataFolder, "config.yml");
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }

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

            if (invitedPlayerName.equalsIgnoreCase(player.getName())) {
                player.sendMessage(msgData.getMessage(MessageKey.SELF_INVITE));
                return;
            }

            // 이미 초대된 플레이어인지 확인
            if (invitedValid.isInvitedPlayer(invitedPlayerName)) {
                String message = msgData.getMessage(MessageKey.ALREADY_INVITED);
                String formattedMessage = message.replace("{player}", invitedPlayerName);
                player.sendMessage(formattedMessage);
                return;
            }

            // 초대 제한 확인 및 초대 수 증가
            int inviteLimit = config.getInt("InviteSetting.InviteLimit");
            int playerInvitedCount = invitedValid.getInvitesCount(player.getName());

            if (playerInvitedCount >= inviteLimit) {
                player.sendMessage(msgData.getMessage(MessageKey.MAX_INVITES_REACHED));
                return;
            }

            // 초대 등록 및 메시지 전송
            invitedValid.registerInvite(player.getName(), invitedPlayerName);
            updateInviteCount(player.getName());
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
        if (invitedUsers.isEmpty()) {
            player.sendMessage(msgData.getMessage(MessageKey.NO_INVITED_PLAYERS));
            return;
        }
        int inviteLimit = config.getInt("InviteSetting.InviteLimit");
        int playerInvitedCount = invitedValid.getInvitesCount(player.getName());
        String message = msgData.getMessage(MessageKey.INVITED_PLAYER);
        String formattedMessage = message.replace("{int}", String.valueOf(playerInvitedCount))
                .replace("{max}", String.valueOf(inviteLimit));
        player.sendMessage(formattedMessage);
        invitedUsers.forEach(invitedUser -> player.sendMessage("- " + invitedUser));
    }

    private void updateInviteCount(String inviter) {
        invitedValid.updateInviteCount(inviter);
    }

    private @NotNull List<String> getInvitedUsers(String inviterName) {
        List<String> invitedUsers = new ArrayList<>();
        for (Map.Entry<String, String> entry : invitedValid.getInvites().entrySet()) {
            if (entry.getValue().equals(inviterName)) {
                invitedUsers.add(entry.getKey());
            }
        }
        return invitedUsers;
    }
}