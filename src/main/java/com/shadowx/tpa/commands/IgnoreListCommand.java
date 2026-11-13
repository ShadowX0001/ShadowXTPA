package com.shadowx.tpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class IgnoreListCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public IgnoreListCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.ignore")) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        String path = "ignored." + player.getUniqueId();

        List<String> ignored =
                plugin.getConfig().getStringList(path);

        player.sendMessage(color("&8&m--------&r &6Ignored Players &8&m--------"));

        if (ignored.isEmpty()) {
            player.sendMessage(color("&7Your ignore list is empty!"));
            return true;
        }

        int number = 1;

        for (String id : ignored) {

            try {
                UUID uuid = UUID.fromString(id);

                OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);

                String name = target.getName();

                if (name == null) {
                    name = id;
                }

                player.sendMessage(color("&e" + number
                        + ". &f" + name));

                number++;

            } catch (IllegalArgumentException exception) {
                continue;
            }
        }

        player.sendMessage(color("&8&m-------------------------------"));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
