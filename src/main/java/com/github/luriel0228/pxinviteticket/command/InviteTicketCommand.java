package com.github.luriel0228.pxinviteticket.command;

import com.github.luriel0228.pxinviteticket.PXInviteTicket;
import com.github.luriel0228.pxinviteticket.message.Message;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.message.MessageKey;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import com.github.luriel0228.pxinviteticket.valid.PermissionValid;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                case "초대권등록":
                    handleRegisterInviteItemCommand(player);
                    break;
                case "초대권지급":
                    handleGiveInviteCommand(player, args);
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

            ItemStack inviteItem = getInviteItem();
            if (!player.getInventory().containsAtLeast(inviteItem, 1)) {
                player.sendMessage(msgData.getMessage(MessageKey.NO_INVITE_ITEM));
                return;
            }

            if (invitedPlayerName.equalsIgnoreCase(player.getName())) {
                player.sendMessage(msgData.getMessage(MessageKey.SELF_INVITE));
                return;
            }

            if (invitedValid.isInvitedPlayer(invitedPlayerName)) {
                String message = msgData.getMessage(MessageKey.ALREADY_INVITED);
                String formattedMessage = message.replace("{player}", invitedPlayerName);
                player.sendMessage(formattedMessage);
                return;
            }

            int inviteLimit = config.getInt("InviteSetting.InviteLimit");
            int playerInvitedCount = invitedValid.getInvitesCount(player.getName());

            if (playerInvitedCount >= inviteLimit) {
                player.sendMessage(msgData.getMessage(MessageKey.MAX_INVITES_REACHED));
                return;
            }

            invitedValid.registerInvite(player.getName(), invitedPlayerName);
            updateInviteCount(player.getName());
            player.getInventory().removeItem(inviteItem);
            String message = msgData.getMessage(MessageKey.SET_INVITE_SUCCESS);
            String formattedMessage = message.replace("{player}", invitedPlayerName);
            player.sendMessage(formattedMessage);

        } else {
            player.sendMessage(msgData.getMessage(MessageKey.MISSING_PLAYER));
        }
    }

    private void handleRegisterInviteItemCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (PermissionValid.hasPermission(player, "admin")) {
            if (itemInHand.getType() == Material.AIR) {
                player.sendMessage(msgData.getMessage(MessageKey.EMPTY_HAND));
                return;
            }

            saveInviteItem(itemInHand);
            player.sendMessage(msgData.getMessage(MessageKey.INVITE_ITEM_REGISTERED));
        }
    }

    private void saveInviteItem(ItemStack inviteItem) {
        ConfigurationSection configSection = getConfigSection("InviteSetting.InviteItem");

        configSection.set("material", inviteItem.getType().name());

        ItemMeta itemMeta = inviteItem.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            configSection.set("name", itemMeta.getDisplayName());
        }

        if (itemMeta != null && itemMeta.hasLore()) {
            configSection.set("lore", itemMeta.getLore());
        }

        plugin.saveConfig();
    }

    private ConfigurationSection getConfigSection(String path) {
        ConfigurationSection configSection = plugin.getConfig().getConfigurationSection(path);
        if (configSection == null) {
            configSection = plugin.getConfig().createSection(path);
        }
        return configSection;
    }

    private void handleGiveInviteCommand(Player player, String @NotNull [] args) {
        if (PermissionValid.hasPermission(player, "admin")) {
            String targetPlayerName;

            if (args.length > 1) {
                targetPlayerName = args[1];
            } else {
                targetPlayerName = player.getName();
            }

            ItemStack inviteItem = loadInviteItem();
            Player targetPlayer = plugin.getServer().getPlayerExact(targetPlayerName);

            if (targetPlayer != null) {
                targetPlayer.getInventory().addItem(inviteItem);
                String message = msgData.getMessage(MessageKey.GIVE_INVITE_SUCCESS);
                String formattedMessage = message.replace("{player}", targetPlayerName);
                player.sendMessage(formattedMessage);
            } else {
                player.sendMessage(msgData.getMessage(MessageKey.INVALID_PLAYER));
            }
        } else {
            player.sendMessage(msgData.getMessage(MessageKey.NO_PERMISSION));
        }
    }

    private ItemStack loadInviteItem() {
        ConfigurationSection configSection = getConfigSection("InviteSetting.InviteItem");
        Material material = Material.matchMaterial(configSection.getString("material", "PAPER"));

        if (material == null) {
            plugin.getLogger().warning(msgData.getMessage(MessageKey.NO_INVITE_ITEM_SET));
            return createDefaultInviteItem();
        }

        ItemStack inviteItem = new ItemStack(material);
        ItemMeta itemMeta = inviteItem.getItemMeta();

        if (configSection.contains("name")) {
            Objects.requireNonNull(itemMeta).setDisplayName(configSection.getString("name"));
        }

        if (configSection.contains("lore")) {
            List<String> lore = configSection.getStringList("lore");
            Objects.requireNonNull(itemMeta).setLore(lore);
        }

        inviteItem.setItemMeta(itemMeta);
        return inviteItem;
    }

    private ItemStack createDefaultInviteItem() {
        ItemStack defaultInviteItem = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = defaultInviteItem.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName("초대권");
        defaultInviteItem.setItemMeta(itemMeta);
        return defaultInviteItem;
    }

    private ItemStack getInviteItem() {
        return new ItemStack(Material.DIAMOND); // 예시로 다이아몬드를 초대권 아이템으로 사용
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