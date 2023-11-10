package com.github.luriel0228.pxinviteticket.command.tabcomplete;

import com.github.luriel0228.pxinviteticket.valid.PermissionValid;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InviteTicketTab implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (label.equalsIgnoreCase("초대")) {
                if (PermissionValid.hasPermission((Player) sender, "리로드")) completions.add("리로드");
                completions.add("등록");
                completions.add("목록");

            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}