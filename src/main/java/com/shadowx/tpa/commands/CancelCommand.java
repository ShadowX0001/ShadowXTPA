package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.TPARequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelCommand implements CommandExecutor {

    private final RequestManager requestManager;

    public CancelCommand(RequestManager requestManager) {
        this.requestManager = requestManager;
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

        int cancelled = 0;

        for (Player target : Bukkit.getOnlinePlayers()) {

            TPARequest request = requestManager.getRequest(
                    player.getUniqueId(),
                    target.getUniqueId()
            );

            if (request == null) {
                continue;
            }

            requestManager.removeRequest(
                    player.getUniqueId(),
                    target.getUniqueId()
            );

            target.sendMessage(color("&c" + player.getName()
                    + " cancelled their TPA request!"));

            cancelled++;
        }

        if (cancelled == 0) {
            player.sendMessage(color("&cYou don't have any pending requests!"));
            return true;
        }

        player.sendMessage(color("&aSuccessfully cancelled &e"
                + cancelled + "&a pending request(s)!"));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
