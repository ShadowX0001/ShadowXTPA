package com.shadowx.tpa.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ToggleCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.toggle")) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        String path = "disabled-requests." + player.getUniqueId();

        boolean disabled = plugin.getConfig().getBoolean(path, false);

        plugin.getConfig().set(path, !disabled);
        plugin.saveConfig();

        if (!disabled) {
            player.sendMessage(color("&cIncoming TPA requests are now OFF!"));
        } else {
            player.sendMessage(color("&aIncoming TPA requests are now ON!"));
        }

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
