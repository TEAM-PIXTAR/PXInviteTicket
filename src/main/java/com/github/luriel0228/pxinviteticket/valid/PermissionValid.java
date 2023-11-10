package com.github.luriel0228.pxinviteticket.valid;

import com.github.luriel0228.pxinviteticket.message.Message;
import com.github.luriel0228.pxinviteticket.message.MessageKey;
import org.bukkit.entity.Player;

public class PermissionValid {

    public static Boolean hasPermission(Player player, String permission) {
        Message msgData = Message.getInstance();
        if (player.hasPermission("px.inviteticket." + permission)) {
            return true;
        } else {
            player.sendMessage(msgData.getMessage(MessageKey.NO_PERMISSION));
            return false;
        }
    }
}