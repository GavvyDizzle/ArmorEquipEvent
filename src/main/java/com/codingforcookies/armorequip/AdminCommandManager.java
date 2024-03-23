package com.codingforcookies.armorequip;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple command to reload the config file
 */
public class AdminCommandManager implements TabExecutor {

    private final JavaPlugin instance;
    private final ArmorListener armorListener;
    private final List<String> subcommands = List.of("reload");

    public AdminCommandManager(JavaPlugin instance, ArmorListener armorListener) {
        this.instance = instance;
        this.armorListener = armorListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "No subcommand provided");
            return true;
        }

        if (args[0].equals("reload")) {
            instance.reloadConfig();
            armorListener.reload();
            sender.sendMessage(ChatColor.GREEN + "[ArmorEquipEvent] Reloaded interact blocks");
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid subcommand provided");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], subcommands, list);
            return list;
        }
        return Collections.emptyList();
    }
}
