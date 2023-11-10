package com.github.luriel0228.pxinviteticket.command;

import com.github.luriel0228.pxinviteticket.PXInviteTicket;
import com.github.luriel0228.pxinviteticket.message.Message;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.message.MessageKey;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import com.github.luriel0228.pxinviteticket.valid.PermissionValid;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InviteTicketCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final InvitedValid invitedValid;

    public InviteTicketCommand(PXInviteTicket plugin, InvitedValid invitedValid) {
        this.plugin = plugin;
        this.invitedValid = invitedValid;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Message msgData = Message.getInstance();

        if (!(sender instanceof Player)) {
            sender.sendMessage(msgData.getMessage(MessageKey.PLAYER_ONLY));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(msgData.getMessage(MessageKey.WRONG_COMMAND));
            return true;
        }

        switch (args[0]) {
            case "리로드":
                if (!PermissionValid.hasPermission(player, "reload")) return true;
                plugin.reloadConfig();
                MessageConfig.reload();
                player.sendMessage(msgData.getMessage(MessageKey.RELOAD_CONFIG));
                break;
            case "등록":
                if (args.length > 1) {
                    String invitedPlayerName = args[1];

                    if (invitedPlayerName.isEmpty()) {
                        player.sendMessage(msgData.getMessage(MessageKey.MISSING_PLAYER));
                        return true;
                    }

                    Player invitedPlayer = Bukkit.getPlayerExact(invitedPlayerName);
                    if (invitedPlayer == null) {
                        player.sendMessage(msgData.getMessage(MessageKey.UNKNOWN_PLAYER));
                        return true;
                    }

                    invitedValid.registerInvite(player.getName(), invitedPlayer.getName());
                    String message = msgData.getMessage(MessageKey.SET_INVITE_SUCCESS);
                    String formattedMessage = message.replace("{player}", invitedPlayer.getName());
                    player.sendMessage(formattedMessage);
                } else {
                    player.sendMessage(msgData.getMessage(MessageKey.MISSING_PLAYER));
                }
                break;
            case "목록":
                player.sendMessage(msgData.getMessage(MessageKey.INVITED_PLAYER));
                List<String> invitedUsers = getInvitedUsers(player.getName());
                for (String invitedUser : invitedUsers) {
                    player.sendMessage("- " + invitedUser);
                }
                break;
        }
        return true;
    }

    private List<String> getInvitedUsers(String inviterName) {
        List<String> invitedUsers = new ArrayList<>();
        for (Map.Entry<String, String> entry : invitedValid.getInvites().entrySet()) {
            if (entry.getValue().equals(inviterName)) {
                invitedUsers.add(entry.getKey());
            }
        }
        return invitedUsers;
    }
}