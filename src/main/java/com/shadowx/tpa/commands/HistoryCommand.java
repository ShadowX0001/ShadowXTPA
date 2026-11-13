package com.shadowx.tpa.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HistoryCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.use")) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        String path = "history." + player.getUniqueId();

        List<String> history =
                plugin.getConfig().getStringList(path);

        player.sendMessage(color("&8&m--------&r &6TPA History &8&m--------"));

        if (history.isEmpty()) {
            player.sendMessage(color("&7No TPA history found!"));
            return true;
        }

        for (String entry : history) {
            player.sendMessage(color("&e» &f" + entry));
        }

        player.sendMessage(color("&8&m------------------------------"));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
