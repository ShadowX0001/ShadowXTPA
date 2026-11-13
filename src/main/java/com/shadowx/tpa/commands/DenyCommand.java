package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.SoundManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DenyCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager manager;

    public DenyCommand(JavaPlugin plugin, RequestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("shadowxtpa.deny")) {
            player.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length > 1) {
            player.sendMessage("§cUsage: /tpdeny [player]");
            return true;
        }

        Player target = null;

        if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);
        } else if (manager.getPendingCount(player.getUniqueId()) == 1) {
            var request = manager.getRequests(player.getUniqueId())
                    .iterator().next();

            target = Bukkit.getPlayer(request.getSender());
        }

        if (target == null) {
            player.sendMessage("§cRequest not found!");
            return true;
        }

        if (!manager.removeRequest(
                target.getUniqueId(),
                player.getUniqueId())) {

            player.sendMessage("§cNo request from that player!");
            return true;
        }

        player.sendMessage("§cRequest denied!");

        target.sendMessage("§cYour request was denied by "
                + player.getName());

        manager.addHistory(player.getUniqueId(),
                "Denied request from " + target.getName());

        manager.addHistory(target.getUniqueId(),
                "Request denied by " + player.getName());

        SoundManager.play(plugin, player, "request-denied");
        SoundManager.play(plugin, target, "request-denied");

        return true;
    }
}
