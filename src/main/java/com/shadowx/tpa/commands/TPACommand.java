package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.RequestType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TPACommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager requestManager;

    public TPACommand(JavaPlugin plugin, RequestManager requestManager) {
        this.plugin = plugin;
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
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        boolean isHere = command.getName().equalsIgnoreCase("tpahere");

        String permission = isHere
                ? "shadowxtpa.tpahere"
                : "shadowxtpa.tpa";

        if (!player.hasPermission(permission)) {
            player.sendMessage(color("&cYou don't have permission!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(color(
                    "&cUsage: /" + label + " <player>"
            ));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            player.sendMessage(color("&cPlayer not found or offline!"));
            return true;
        }

        if (player.equals(target)) {
            player.sendMessage(color("&cYou cannot send a request to yourself!"));
            return true;
        }

        int timeout = plugin.getConfig().getInt(
                "settings.request-timeout", 60
        );

        RequestType type = isHere
                ? RequestType.TPAHERE
                : RequestType.TPA;

        boolean success = requestManager.addRequest(
                player,
                target,
                type,
                timeout
        );

        if (!success) {
            player.sendMessage(color(
                    "&cRequest already exists or target's request limit is full!"
            ));
            return true;
        }

        if (isHere) {

            player.sendMessage(color(
                    "&aTPAHere request sent to &e" + target.getName()
            ));

            target.sendMessage(color(
                    "&e" + player.getName()
                    + " &awants you to teleport to them!"
            ));

        } else {

            player.sendMessage(color(
                    "&aTPA request successfully sent to &e"
                    + target.getName()
            ));

            target.sendMessage(color(
                    "&e" + player.getName()
                    + " &awants to teleport to you!"
            ));
        }

        target.sendMessage(color(
                "&aUse &e/tpaccept &aor &c/tpdeny"
        ));

        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
