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

        if (!player.hasPermission("shadowxtpa.tpa")) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        Player target = null;

        if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                player.sendMessage(color("&cPlayer not found or offline!"));
                return true;
            }
        } else if (args.length > 1) {
            player.sendMessage(color("&cUsage: /tpacancel [player]"));
            return true;
        }

        TPARequest selectedRequest = null;

        if (target != null) {
            selectedRequest = requestManager.getRequest(
                    player.getUniqueId(),
                    target.getUniqueId()
            );
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                TPARequest request = requestManager.getRequest(
                        player.getUniqueId(),
                        onlinePlayer.getUniqueId()
                );

                if (request != null) {
                    selectedRequest = request;
                    target = onlinePlayer;
                    break;
                }
            }
        }

        if (selectedRequest == null) {
            player.sendMessage(color("&cYou have no pending request to cancel!"));
            return true;
        }

        if (!requestManager.removeRequest(
                player.getUniqueId(),
                target.getUniqueId())) {

            player.sendMessage(color("&cCould not cancel your request!"));
            return true;
        }

        player.sendMessage(color("&aYour TPA request to &e"
                + target.getName() + " &ahas been cancelled!"));

        target.sendMessage(color("&e" + player.getName()
                + " &chas cancelled their TPA request!"));

        requestManager.addHistory(
                player.getUniqueId(),
                "Cancelled request to " + target.getName()
        );

        requestManager.addHistory(
                target.getUniqueId(),
                player.getName() + " cancelled their request"
        );

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
