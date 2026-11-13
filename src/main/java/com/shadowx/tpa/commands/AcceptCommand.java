package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.TPARequest;
import com.shadowx.tpa.managers.RequestManager.RequestType;
import com.shadowx.tpa.managers.TeleportManager;
import com.shadowx.tpa.managers.SoundManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class AcceptCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager requests;
    private final TeleportManager teleports;

    public AcceptCommand(
            JavaPlugin plugin,
            RequestManager requests,
            TeleportManager teleports
    ) {
        this.plugin = plugin;
        this.requests = requests;
        this.teleports = teleports;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("shadowxtpa.accept")) {
            player.sendMessage(c("&cNo permission!"));
            return true;
        }

        Collection<TPARequest> list =
                requests.getRequests(player.getUniqueId());

        if (list.isEmpty()) {
            player.sendMessage(c("&cNo pending requests!"));
            return true;
        }

        TPARequest selected = null;

        if (args.length == 1) {

            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                player.sendMessage(c("&cPlayer not found!"));
                return true;
            }

            selected = requests.getRequest(
                    target.getUniqueId(),
                    player.getUniqueId()
            );

        } else if (args.length == 0 && list.size() == 1) {

            selected = list.iterator().next();

        } else {

            player.sendMessage(c("&eMultiple requests! Use /tpaccept <player>"));
            return true;
        }

        if (selected == null) {
            player.sendMessage(c("&cRequest not found!"));
            return true;
        }

        Player requester = Bukkit.getPlayer(selected.getSender());

        if (requester == null) {
            requests.removeRequest(
                    selected.getSender(),
                    player.getUniqueId()
            );

            player.sendMessage(c("&cRequester is offline!"));
            return true;
        }

        Player teleporting;
        Player destination;

        if (selected.getType() == RequestType.TPA) {
            teleporting = requester;
            destination = player;
        } else {
            teleporting = player;
            destination = requester;
        }

        if (!plugin.getConfig().getBoolean(
                "settings.allow-cross-world", true)
                && !teleporting.getWorld().equals(destination.getWorld())) {

            player.sendMessage(c("&cCross-world teleport disabled!"));
            return true;
        }

        requests.removeRequest(
                requester.getUniqueId(),
                player.getUniqueId()
        );

        player.sendMessage(c("&aAccepted request from &e"
                + requester.getName()));

        requester.sendMessage(c("&aYour request was accepted!"));

        requests.addHistory(
                player.getUniqueId(),
                "Accepted request from " + requester.getName()
        );

        requests.addHistory(
                requester.getUniqueId(),
                "Request accepted by " + player.getName()
        );

        SoundManager.play(plugin, player, "request-accepted");
        SoundManager.play(plugin, requester, "request-accepted");

        teleports.teleport(teleporting, destination);

        return true;
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
