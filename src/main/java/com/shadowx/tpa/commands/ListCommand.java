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

public class ListCommand implements CommandExecutor {

    private final RequestManager requestManager;

    public ListCommand(RequestManager requestManager) {
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

        Collection<TPARequest> requests =
                requestManager.getRequests(player.getUniqueId());

        player.sendMessage(color("&8&m--------&r &6Pending TPA Requests &8&m--------"));

        if (requests.isEmpty()) {
            player.sendMessage(color("&7You don't have any pending requests!"));
            return true;
        }

        int number = 1;

        for (TPARequest request : requests) {

            Player requester = Bukkit.getPlayer(request.getSender());

            if (requester == null) {
                continue;
            }

            String type = request.getType().name();

            player.sendMessage(color("&e" + number + ". &f"
                    + requester.getName()
                    + " &7- &b" + type));

            number++;
        }

        player.sendMessage(color("&7Use &f/tpaccept <player>"
                + " &7or &f/tpdeny <player>"));

        player.sendMessage(color("&8&m--------------------------------------"));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
