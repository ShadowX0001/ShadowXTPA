package com.shadowx.tpa.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class HistoryCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HistoryCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

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

        player.sendMessage(color("&8&m----------&r &6TPA HISTORY &8&m----------"));

        if (history.isEmpty()) {
            player.sendMessage(color("&7Your TPA history is empty!"));
            player.sendMessage(color("&8&m------------------------------------"));
            return true;
        }

        Collections.reverse(history);

        int maximum = plugin.getConfig()
                .getInt("settings.history-limit", 20);

        int displayed = Math.min(history.size(), maximum);

        for (int i = 0; i < displayed; i++) {
            player.sendMessage(color("&e" + (i + 1)
                    + ". &f" + history.get(i)));
        }

        player.sendMessage(color("&7Showing &e" + displayed
                + " &7history entries."));

        player.sendMessage(color("&8&m------------------------------------"));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
