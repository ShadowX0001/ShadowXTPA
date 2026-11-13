package com.shadowx.tpa.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!sender.hasPermission("shadowxtpa.admin")) {
            sender.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage(color("&cUsage: /tpareload"));
            return true;
        }

        plugin.reloadConfig();

        sender.sendMessage(color("&aShadowXTPA configuration reloaded successfully!"));

        plugin.getLogger().info("Configuration successfully reloaded by "
                + sender.getName());

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
