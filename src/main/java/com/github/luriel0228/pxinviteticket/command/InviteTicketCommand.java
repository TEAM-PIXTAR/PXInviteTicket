package com.github.luriel0228.pxinviteticket.command;

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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class InviteTicketCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final InvitedValid invitedValid;
    private final File settingFile;
    private final FileConfiguration settingConfig;

    public InviteTicketCommand(JavaPlugin plugin, InvitedValid invitedValid) {
        this.plugin = plugin;
        this.invitedValid = invitedValid;
        this.settingFile = new File(plugin.getDataFolder(), "setting.yml");
        this.settingConfig = YamlConfiguration.loadConfiguration(settingFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Message.getInstance().getMessage(MessageKey.PLAYER_ONLY));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Message.getInstance().getMessage(MessageKey.WRONG_COMMAND));
            return true;
        }

        if (label.equalsIgnoreCase("초대")) {
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
                        player.sendMessage(Message.getInstance().getMessage(MessageKey.WRONG_COMMAND));
                        break;
                }
            } catch (SQLException e) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.SQL_ERROR));
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred.", e);
            }
        } else if (label.equalsIgnoreCase("invite") || label.equalsIgnoreCase("it")) {
            try {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        handleReloadCommand(player);
                        break;
                    case "add":
                        handleRegisterCommand(player, args);
                        break;
                    case "list":
                        handleListCommand(player);
                        break;
                    case "setitem":
                        handleRegisterInviteItemCommand(player);
                        break;
                    case "giveitem":
                        handleGiveInviteCommand(player, args);
                        break;
                    default:
                        player.sendMessage(Message.getInstance().getMessage(MessageKey.WRONG_COMMAND));
                        break;
                }
            } catch (SQLException e) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.SQL_ERROR));
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred.", e);
            }
        }

        return true;
    }

    private void handleReloadCommand(Player player) {
        if (PermissionValid.hasPermission(player, "reload")) {
            plugin.reloadConfig();

            MessageConfig.reload();
            player.sendMessage(Message.getInstance().getMessage(MessageKey.RELOAD_CONFIG));
        }
    }

    private void handleRegisterCommand(Player player, String[] args) throws SQLException {
        if (args.length > 1) {
            String invitedPlayerName = args[1];

            if (invitedPlayerName.isEmpty()) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.MISSING_PLAYER));
                return;
            }

            ItemStack inviteItem = getInviteItem();
            if (!player.getInventory().containsAtLeast(inviteItem, 1)) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.NO_INVITE_ITEM));
                return;
            }

            if (invitedPlayerName.equalsIgnoreCase(player.getName())) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.SELF_INVITE));
                return;
            }

            if (invitedValid.isInvitedPlayer(invitedPlayerName.toLowerCase())) {
                String message = Message.getInstance().getMessage(MessageKey.ALREADY_INVITED);
                String formattedMessage = message.replace("{player}", invitedPlayerName);
                player.sendMessage(formattedMessage);
                return;
            }

            int inviteLimit = plugin.getConfig().getInt("InviteSetting.InviteLimit");
            int playerInvitedCount = invitedValid.getInvitesCount(player.getName());

            if (playerInvitedCount >= inviteLimit) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.MAX_INVITES_REACHED));
                return;
            }

            invitedValid.registerInvite(player.getName(), invitedPlayerName);
            updateInviteCount(player.getName());
            player.getInventory().removeItem(inviteItem);
            String message = Message.getInstance().getMessage(MessageKey.SET_INVITE_SUCCESS);
            String formattedMessage = message.replace("{player}", invitedPlayerName);
            player.sendMessage(formattedMessage);

        } else {
            player.sendMessage(Message.getInstance().getMessage(MessageKey.MISSING_PLAYER));
        }
    }

    private void handleRegisterInviteItemCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (PermissionValid.hasPermission(player, "admin")) {
            if (itemInHand.getType() == Material.AIR) {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.EMPTY_HAND));
                return;
            }

            saveInviteItem(itemInHand);
            setInviteItemInHand(itemInHand);
            player.sendMessage(Message.getInstance().getMessage(MessageKey.INVITE_ITEM_REGISTERED));
        }
    }

    private void setInviteItemInHand(ItemStack inviteItem) {
        ConfigurationSection configSection = getSettingSection();

        configSection.set("material", inviteItem.getType().name());

        ItemMeta itemMeta = inviteItem.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName()) {
                configSection.set("name", itemMeta.getDisplayName());
            }

            if (itemMeta.hasLore()) {
                configSection.set("lore", itemMeta.getLore());
            }

            if (itemMeta.hasCustomModelData()) {
                configSection.set("custom-model-data", itemMeta.getCustomModelData());
            }
        }

        saveSettingConfig();
    }

    private void saveInviteItem(ItemStack inviteItem) {
        ConfigurationSection configSection = getSettingSection();

        if (configSection.get("material") != null) {
            return;
        }

        configSection.set("material", inviteItem.getType().name());
        ItemMeta itemMeta = inviteItem.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            configSection.set("name", itemMeta.getDisplayName());
        }
        if (itemMeta != null && itemMeta.hasLore()) {
            configSection.set("lore", itemMeta.getLore());
        }

        if (itemMeta != null && itemMeta.hasCustomModelData()) {
            configSection.set("custom-model-data", itemMeta.getCustomModelData());
        }

        saveSettingConfig();
    }

    public ItemStack getInviteItem() {
        return loadCustomInviteItem();
    }

    private void handleGiveInviteCommand(Player player, String[] args) {
        if (PermissionValid.hasPermission(player, "admin")) {
            String targetPlayerName;

            if (args.length > 1) {
                targetPlayerName = args[1];
            } else {
                targetPlayerName = player.getName();
            }

            ItemStack inviteItem = loadCustomInviteItem();
            Player targetPlayer = plugin.getServer().getPlayerExact(targetPlayerName);

            if (targetPlayer != null) {
                targetPlayer.getInventory().addItem(inviteItem);
                String message = Message.getInstance().getMessage(MessageKey.GIVE_INVITE_SUCCESS);
                String formattedMessage = message.replace("{player}", targetPlayerName);
                player.sendMessage(formattedMessage);
            } else {
                player.sendMessage(Message.getInstance().getMessage(MessageKey.INVALID_PLAYER));
            }
        } else {
            player.sendMessage(Message.getInstance().getMessage(MessageKey.NO_PERMISSION));
        }
    }

    private void saveSettingConfig() {
        try {
            settingConfig.save(settingFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save setting.yml", e);
        }
    }

    private void handleListCommand(Player player) throws SQLException {
        List<String> invitedUsers = getInvitedUsers(player.getName());
        if (invitedUsers.isEmpty()) {
            player.sendMessage(Message.getInstance().getMessage(MessageKey.NO_INVITED_PLAYERS));
            return;
        }

        int inviteLimit = plugin.getConfig().getInt("InviteSetting.InviteLimit");
        int playerInvitedCount = invitedValid.getInvitesCount(player.getName());
        String message = Message.getInstance().getMessage(MessageKey.INVITED_PLAYER);
        String formattedMessage = message.replace("{int}", String.valueOf(playerInvitedCount))
                .replace("{max}", String.valueOf(inviteLimit));
        player.sendMessage(formattedMessage);

        invitedUsers.forEach(invitedUser -> player.sendMessage("- " + invitedUser));
    }

    private void updateInviteCount(String inviter) {
        invitedValid.updateInviteCount(inviter);
    }

    private List<String> getInvitedUsers(String inviterName) {
        return invitedValid.getInvites().entrySet().stream()
                .filter(entry -> entry.getValue().equals(inviterName))
                .map(Map.Entry::getKey)
                .toList();
    }

    private ConfigurationSection getSettingSection() {
        ConfigurationSection settingSection = settingConfig.getConfigurationSection("InviteSetting");
        if (settingSection == null) {
            settingSection = settingConfig.createSection("InviteSetting");
        }

        ConfigurationSection inviteItemSection = settingSection.getConfigurationSection("InviteItem");
        if (inviteItemSection == null) {
            inviteItemSection = settingSection.createSection("InviteItem");
        }

        return inviteItemSection;
    }

    public ItemStack loadCustomInviteItem() {
        ConfigurationSection configSection = getSettingSection();
        Material material = Material.matchMaterial(configSection.getString("material", "PAPER"));

        if (material == null) {
            plugin.getLogger().warning("No valid material set for invite item.");
            return null;
        }

        ItemStack inviteItem = new ItemStack(material);
        ItemMeta itemMeta = inviteItem.getItemMeta();

        if (configSection.contains("name")) {
            if (itemMeta != null) {
                itemMeta.setDisplayName(configSection.getString("name"));
            }
        }

        if (configSection.contains("lore")) {
            if (itemMeta != null) {
                List<String> lore = configSection.getStringList("lore");
                itemMeta.setLore(lore);
            }
        }

        if (configSection.contains("custom-model-data")) {
            if (itemMeta != null) {
                itemMeta.setCustomModelData(configSection.getInt("custom-model-data"));
            }
        }

        inviteItem.setItemMeta(itemMeta);
        return inviteItem;
    }

}
