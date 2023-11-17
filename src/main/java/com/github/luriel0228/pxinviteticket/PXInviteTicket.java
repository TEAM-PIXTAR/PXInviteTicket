package com.github.luriel0228.pxinviteticket;

import com.github.luriel0228.pxinviteticket.command.InviteTicketCommand;
import com.github.luriel0228.pxinviteticket.command.tabcomplete.InviteTicketTab;
import com.github.luriel0228.pxinviteticket.listeners.InviteTicketListener;
import com.github.luriel0228.pxinviteticket.listeners.PlayerJoinListener;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.file.DataFile;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
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
        config = getConfig();
        saveDefaultConfig();
        dataFile = new DataFile("PXInviteTicket.db");
        MessageConfig.setup();
        File settingFile = new File(getDataFolder(), "setting.yml");
        if (!settingFile.exists()) {
            saveResource("setting.yml", false);
        }
        /* --------------- COMMAND ---------------*/
        if (config != null && config.getBoolean("InviteTicket.EnablePlugin")) {
            setExecutor();
            registerEvent(config);
        } else {
            getLogger().info("플러그인이 비활성화 상태입니다. 활성화하려면 config.yml에서 `EnablePlugin: true`로 설정한 후 서버를 재시작 해주십시오.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        dataFile.closeConnection();
    }

    private void setExecutor() {
        instance = this;
        PluginCommand returnTicketCmd = getCommand("초대");
        returnTicketCmd.setAliases(Arrays.asList("invite", "it"));
        this.invitedValid = new InvitedValid(dataFile);
        Objects.requireNonNull(returnTicketCmd).setExecutor(new InviteTicketCommand(this, this.invitedValid));
        returnTicketCmd.setTabCompleter(new InviteTicketTab());
    }

    public void registerEvent(FileConfiguration config) {
        this.config = config;
        getServer().getPluginManager().registerEvents(new InviteTicketListener(this, invitedValid), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(invitedValid, config), this);
    }
}

