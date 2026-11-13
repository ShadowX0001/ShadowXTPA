package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.TPARequest;
import com.shadowx.tpa.managers.RequestManager.RequestType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

public class AcceptCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager requestManager;

    public AcceptCommand(JavaPlugin plugin, RequestManager requestManager) {
        this.plugin = plugin;
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.accept")) {
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
                player.sendMessage(color("&7Use /tpaccept <player>"));
                return true;
            }

            selectedRequest = requests.iterator().next();

        } else {
            player.sendMessage(color("&cUsage: /tpaccept [player]"));
            return true;
        }

        UUID requesterId = selectedRequest.getSender();

        Player requester = Bukkit.getPlayer(requesterId);

        if (requester == null) {
            requestManager.removeRequest(
                    requesterId,
                    player.getUniqueId()
            );

            player.sendMessage(color("&cThat player is offline!"));
            return true;
        }

        RequestType type = selectedRequest.getType();

        Player teleportingPlayer;
        Player destinationPlayer;

        if (type == RequestType.TPA) {
            teleportingPlayer = requester;
            destinationPlayer = player;
        } else {
            teleportingPlayer = player;
            destinationPlayer = requester;
        }

        boolean allowCrossWorld = plugin.getConfig()
                .getBoolean("settings.allow-cross-world", true);

        if (!allowCrossWorld &&
                !teleportingPlayer.getWorld().equals(destinationPlayer.getWorld())) {

            player.sendMessage(color("&cCross-world teleport is disabled!"));
            return true;
        }

        requestManager.removeRequest(
                requesterId,
                player.getUniqueId()
        );

        player.sendMessage(color("&aYou accepted &e"
                + requester.getName() + "&a's request!"));

        requester.sendMessage(color("&aYour request was accepted by &e"
                + player.getName() + "&a!"));

        requestManager.addHistory(
                player.getUniqueId(),
                "Accepted " + type + " request from " + requester.getName()
        );

        requestManager.addHistory(
                requester.getUniqueId(),
                "Request accepted by " + player.getName()
        );

        int delay = plugin.getConfig()
                .getInt("settings.teleport-delay", 5);

        if (teleportingPlayer.hasPermission("shadowxtpa.bypass.delay")) {
            delay = 0;
        }

        if (delay <= 0) {
            teleportPlayer(teleportingPlayer, destinationPlayer);
            return true;
        }

        teleportingPlayer.sendMessage(color(
                "&aTeleporting in &e" + delay + " seconds&a!"
        ));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (!teleportingPlayer.isOnline()
                    || !destinationPlayer.isOnline()) {
                return;
            }

            teleportPlayer(teleportingPlayer, destinationPlayer);

        }, delay * 20L);

        return true;
    }

    private void teleportPlayer(Player player, Player target) {

        if (!player.isOnline() || !target.isOnline()) {
            return;
        }

        Location destination = target.getLocation();

        player.teleport(destination);

        player.sendMessage(color("&aTeleported to &e"
                + target.getName() + "&a!"));

        target.sendMessage(color("&e" + player.getName()
                + " &ahas teleported to you!"));
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
