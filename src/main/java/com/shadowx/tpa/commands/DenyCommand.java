package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.TPARequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DenyCommand implements CommandExecutor {

    private final RequestManager requestManager;

    public DenyCommand(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.deny")) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        Collection<TPARequest> requests =
                requestManager.getRequests(player.getUniqueId());

        if (requests.isEmpty()) {
            player.sendMessage(color("&cNo pending TPA requests!"));
            return true;
        }

        TPARequest selectedRequest;

        if (args.length == 1) {

            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                player.sendMessage(color("&cPlayer not found or offline!"));
                return true;
            }

            selectedRequest = requestManager.getRequest(
                    target.getUniqueId(),
                    player.getUniqueId()
            );

            if (selectedRequest == null) {
                player.sendMessage(color("&cNo request from that player!"));
                return true;
            }

        } else if (args.length == 0) {

            if (requests.size() > 1) {
                player.sendMessage(color("&eMultiple requests found!"));
                player.sendMessage(color("&7Use /tpdeny <player>"));
                return true;
            }

            selectedRequest = requests.iterator().next();

        } else {
            player.sendMessage(color("&cUsage: /tpdeny [player]"));
            return true;
        }

        Player requester = Bukkit.getPlayer(
                selectedRequest.getSender()
        );

        if (!requestManager.removeRequest(
                selectedRequest.getSender(),
                player.getUniqueId())) {
            player.sendMessage(color("&cRequest could not be removed!"));
            return true;
        }

        player.sendMessage(color("&cYou denied the TPA request!"));

        requestManager.addHistory(
                player.getUniqueId(),
                "Denied request from "
                        + (requester != null ? requester.getName() : "Unknown")
        );

        if (requester != null) {

            requester.sendMessage(color("&cYour TPA request was denied by &e"
                    + player.getName() + "&c!"));

            requestManager.addHistory(
                    requester.getUniqueId(),
                    "Request denied by " + player.getName()
            );
        }

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
