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
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        Collection<TPARequest> requests =
                requestManager.getRequests(player.getUniqueId());

        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW
                    + "You don't have any pending TPA requests.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "========== "
                + ChatColor.YELLOW + "Pending TPA Requests"
                + ChatColor.GOLD + " ==========");

        for (TPARequest request : requests) {

            Player requester = Bukkit.getPlayer(
                    request.getSender()
            );

            if (requester == null) {
                continue;
            }

            String requestType = request.getType().name();

            player.sendMessage(
                    ChatColor.GREEN + requester.getName()
                    + ChatColor.GRAY + " | "
                    + ChatColor.AQUA + requestType
                    + ChatColor.GRAY + " | "
                    + ChatColor.YELLOW + "/tpaccept "
                    + requester.getName()
            );
        }

        player.sendMessage(ChatColor.GOLD
                + "======================================");

        return true;
    }
}
