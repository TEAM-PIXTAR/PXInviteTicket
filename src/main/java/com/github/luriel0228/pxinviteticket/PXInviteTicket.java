package com.github.luriel0228.pxinviteticket;

import com.github.luriel0228.pxinviteticket.command.InviteTicketCommand;
import com.github.luriel0228.pxinviteticket.command.tabcomplete.InviteTicketTab;
import com.github.luriel0228.pxinviteticket.listeners.PlayerJoinListener;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.file.DataFile;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PXInviteTicket extends JavaPlugin {

    public DataFile dataFile;
    private FileConfiguration config;
    private InvitedValid invitedValid;

    @Getter
    private static PXInviteTicket instance;

    @Override
    public void onEnable() {
        /*--------------- CONFIG ---------------*/
        saveDefaultConfig();
        dataFile = new DataFile("database.db");
        MessageConfig.setup();

        /* --------------- COMMAND ---------------*/
        if (config != null && Boolean.parseBoolean(config.getString("InviteTicket.EnablePlugin"))) {
            setExecutor();
            registerEvent();
        } else {
            Bukkit.getConsoleSender().sendMessage("플러그인이 비활성화 상태입니다. 활성화 하실려면 config.yml에서 `EnablePlugin: true`로 설정한 후 플러그인을 리로드 하십시오.");
        }
    }

    private void setExecutor() {
        instance = this;
        PluginCommand returnTicketCmd = getCommand("초대");
        InvitedValid invitedValid = new InvitedValid(dataFile);
        Objects.requireNonNull(returnTicketCmd).setExecutor(new InviteTicketCommand(this, invitedValid));
        returnTicketCmd.setTabCompleter(new InviteTicketTab());
    }

    public void registerEvent() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(invitedValid), this);
    }
}

