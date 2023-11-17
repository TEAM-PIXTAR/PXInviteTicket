package com.github.luriel0228.pxinviteticket.listeners;

import com.github.luriel0228.pxinviteticket.command.InviteTicketCommand;
import com.github.luriel0228.pxinviteticket.message.Message;
import com.github.luriel0228.pxinviteticket.message.MessageKey;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class InviteTicketListener implements Listener {

    private final JavaPlugin plugin;
    private final InvitedValid invitedValid;
    private final Map<Player, String> inviteConfirmationMap;

    public InviteTicketListener(JavaPlugin plugin, InvitedValid invitedValid) {
        this.plugin = plugin;
        this.invitedValid = invitedValid;
        this.inviteConfirmationMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == getInviteItem().getType()) {
            handleInviteStart(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (inviteConfirmationMap.containsKey(player)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null) {
                String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

                if (itemName.equals("예")) {
                    String invitedPlayerName = inviteConfirmationMap.get(player);
                    handleInvite(player, invitedPlayerName);
                } else if (itemName.equals("아니요")) {
                    player.sendMessage(Message.getInstance().getMessage(MessageKey.CANCEL_INVITE));
                    handleInviteCancel(player);
                }
            }

            player.closeInventory();

            inviteConfirmationMap.remove(player);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (inviteConfirmationMap.containsKey(player)) {
            String invitedPlayerName = event.getMessage();

            player.sendMessage(ChatColor.GREEN + invitedPlayerName + " 님이 맞으십니까? ");

            event.setCancelled(true);

            inviteConfirmationMap.put(player, invitedPlayerName);

            openConfirmationGUI(player);
        }
    }

    private void handleInviteStart(Player player) {

        player.sendMessage(Message.getInstance().getMessage(MessageKey.GET_INVITE));

        inviteConfirmationMap.put(player, "");
    }

    private void handleInviteCancel(Player player) {
        // 초대 취소 시 초대권을 제거
        ItemStack inviteItem = getInviteItem();
        if (player.getInventory().containsAtLeast(inviteItem, 1)) {
            player.getInventory().removeItem(inviteItem);
        }
    }

    private void handleInvite(Player inviter, String invitedPlayerName) {
        String command = "초대 등록 " + invitedPlayerName;
        Bukkit.dispatchCommand((CommandSender) inviter, command);
    }

    private void openConfirmationGUI(Player player) {

        Inventory gui = Bukkit.createInventory(player, 9, ChatColor.GREEN + "초대 확인");

        ItemStack yesButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta yesMeta = yesButton.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "예");
        yesButton.setItemMeta(yesMeta);

        ItemStack noButton = new ItemStack(Material.RED_WOOL);
        ItemMeta noMeta = noButton.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "아니요");
        noButton.setItemMeta(noMeta);

        gui.setItem(3, yesButton);
        gui.setItem(5, noButton);

        player.openInventory(gui);
    }

    private ItemStack getInviteItem() {
        return new InviteTicketCommand(plugin, invitedValid).getInviteItem();
    }
}