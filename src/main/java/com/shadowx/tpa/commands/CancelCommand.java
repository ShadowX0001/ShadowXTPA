package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.SoundManager;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CancelCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager manager;

    public CancelCommand(JavaPlugin plugin, RequestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("shadowxtpa.tpa")) {
            player.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length > 1) {
            player.sendMessage("§cUsage: /tpacancel [player]");
            return true;
        }

        int cancelled = 0;

        for (Player target : Bukkit.getOnlinePlayers()) {

            if (args.length == 1 &&
                    !target.getName().equalsIgnoreCase(args[0])) {
                continue;
            }

            if (manager.removeRequest(
                    player.getUniqueId(),
                    target.getUniqueId())) {

                cancelled++;

                target.sendMessage("§c" + player.getName()
                        + " cancelled their request.");

                manager.addHistory(target.getUniqueId(),
                        player.getName() + " cancelled their request");

                SoundManager.play(plugin, target, "request-cancelled");
            }
        }

        if (cancelled == 0) {
            player.sendMessage("§cNo pending requests found!");
            return true;
        }

        player.sendMessage("§aCancelled " + cancelled + " request(s)!");

        manager.addHistory(player.getUniqueId(),
                "Cancelled " + cancelled + " request(s)");

        SoundManager.play(plugin, player, "request-cancelled");

        return true;
    }
}
