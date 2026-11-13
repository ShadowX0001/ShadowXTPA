package com.shadowx.tpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class IgnoreCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public IgnoreCommand(JavaPlugin plugin) {
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

        if (args.length != 1) {
            player.sendMessage(color("&cUsage: /tpignore <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            player.sendMessage(color("&cPlayer not found or offline!"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(color("&cYou cannot ignore yourself!"));
            return true;
        }

        String path = "ignored." + player.getUniqueId();

        List<String> ignored = new ArrayList<>(
                plugin.getConfig().getStringList(path)
        );

        String targetId = target.getUniqueId().toString();

        if (ignored.contains(targetId)) {

            ignored.remove(targetId);

            plugin.getConfig().set(path, ignored);
            plugin.saveConfig();

            player.sendMessage(color("&aYou unignored &e"
                    + target.getName() + "&a!"));

        } else {

            ignored.add(targetId);

            plugin.getConfig().set(path, ignored);
            plugin.saveConfig();

            player.sendMessage(color("&aYou are now ignoring &e"
                    + target.getName() + "&a!"));
        }

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
