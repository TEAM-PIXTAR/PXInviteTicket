package com.github.luriel0228.pxinviteticket;

import com.github.luriel0228.pxinviteticket.command.InviteTicketCommand;
import com.github.luriel0228.pxinviteticket.command.tabcomplete.InviteTicketTab;
import com.github.luriel0228.pxinviteticket.message.MessageConfig;
import com.github.luriel0228.pxinviteticket.file.DataFile;
import com.github.luriel0228.pxinviteticket.valid.InvitedValid;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PXInviteTicket extends JavaPlugin {

    private static PXInviteTicket instance;
    public DataFile dataFile;

    @Override
    public void onEnable() {
        /*--------------- CONFIG ---------------*/
        saveDefaultConfig();
        dataFile = new DataFile("database.db");
        MessageConfig.setup();

        /* --------------- COMMAND ---------------*/
        setExecutor();
    }

    private void setExecutor() {
        instance = this;
        PluginCommand returnTicketCmd = getCommand("초대");
        InvitedValid invitedValid = new InvitedValid(dataFile);
        Objects.requireNonNull(returnTicketCmd).setExecutor(new InviteTicketCommand(this, invitedValid));
        returnTicketCmd.setTabCompleter(new InviteTicketTab());
    }

    public static PXInviteTicket getInstance() {
        return instance;
    }
}

