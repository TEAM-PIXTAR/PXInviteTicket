package com.github.luriel0228.pxinviteticket.listeners;

import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import com.github.luriel0228.pxinviteticket.valid.PermissionValid;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerJoinListener implements Listener {

    private final InvitedValid invitedValid;
    private FileConfiguration config;

    public PlayerJoinListener(InvitedValid inviteManager) {
        this.invitedValid = inviteManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            handlePlayerLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        String playerName = event.getPlayer().getName();

        if (PermissionValid.hasPermission(event.getPlayer(), "bypass")) {
            return;
        }

        if (!invitedValid.hasReceivedInvite(playerName)) {
            kickPlayer(event, config.getString("InviteSetting.kick_message"));
        }
    }

    private void kickPlayer(PlayerLoginEvent event, String kickMessage) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
    }
}
