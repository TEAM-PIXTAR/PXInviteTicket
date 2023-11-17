package com.github.luriel0228.pxinviteticket.listeners;

import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import com.github.luriel0228.pxinviteticket.valid.PermissionValid;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerJoinListener implements Listener {

    private final InvitedValid invitedValid;
    private final FileConfiguration config;

    public PlayerJoinListener(InvitedValid invitedValid, FileConfiguration config) {
        this.invitedValid = invitedValid;
        this.config = config;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        try {
            handlePlayerLogin(event, playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePlayerLogin(PlayerLoginEvent event, String playerName) {
        if (PermissionValid.hasPermission(event.getPlayer(), "bypass")) {
            return;
        }

        if (!invitedValid.isInvitedPlayer(playerName.toLowerCase())) {
            kickPlayer(event, config.getString("InviteSetting.kick_message"));
        }
    }


    private void kickPlayer(PlayerLoginEvent event, String kickMessage) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
    }
}