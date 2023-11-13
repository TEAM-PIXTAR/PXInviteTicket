package com.github.luriel0228.pxinviteticket.command.tabcomplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InviteTicketTab implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && label.equalsIgnoreCase("초대")) {
            if (sender.hasPermission("px.inviteticket.reload")) {
                completions.add("리로드");
            }

            if (sender.hasPermission("px.inviteticket.admin")) {
                completions.addAll(Arrays.asList("초대권등록", "초대권지급"));
            }

            completions.addAll(Arrays.asList("등록", "목록"));

        } else if ((args.length == 1 && label.equalsIgnoreCase("invite")) || (args.length == 1 && label.equalsIgnoreCase("it"))) {
            if (sender.hasPermission("px.inviteticket.reload")) {
                completions.add("reload");
            }

            if (sender.hasPermission("px.inviteticket.admin")) {
                completions.addAll(Arrays.asList("setitem", "giveitem"));
            }

            completions.addAll(Arrays.asList("add", "list"));
        }

        return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
    }
}