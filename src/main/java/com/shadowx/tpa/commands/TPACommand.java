package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.RequestType;
import com.shadowx.tpa.managers.SoundManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TPACommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager manager;

    public TPACommand(JavaPlugin plugin, RequestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        boolean here = command.getName().equalsIgnoreCase("tpahere");

        String permission = here
                ? "shadowxtpa.tpahere"
                : "shadowxtpa.tpa";

        if (!player.hasPermission(permission)) {
            player.sendMessage(c("&cNo permission!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(c("&cUsage: /" + label + " <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || target.equals(player)) {
            player.sendMessage(c("&cInvalid player!"));
            return true;
        }

        if (!manager.isRequestsEnabled(target.getUniqueId())) {
            player.sendMessage(c("&cThat player has requests disabled!"));
            return true;
        }

        if (manager.isIgnored(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(c("&cThat player is ignoring you!"));
            return true;
        }

        if (manager.hasRequest(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(c("&cRequest already exists!"));
            return true;
        }

        if (!player.hasPermission("shadowxtpa.bypass.cooldown")) {
            long remaining = manager.getCooldownRemaining(player.getUniqueId());

            if (remaining > 0) {
                player.sendMessage(c("&cWait " + remaining + " seconds!"));
                return true;
            }
        }

        int timeout = plugin.getConfig()
                .getInt("settings.request-timeout", 60);

        RequestType type = here
                ? RequestType.TPAHERE
                : RequestType.TPA;

        if (!manager.addRequest(player, target, type, timeout)) {
            player.sendMessage(c("&cRequest could not be sent!"));
            return true;
        }

        player.sendMessage(c("&aRequest sent to &e" + target.getName()));

        target.sendMessage(c("&e" + player.getName()
                + (here
                ? " &awants you to teleport to them!"
                : " &awants to teleport to you!")));

        SoundManager.play(plugin, target, "request-received");
        SoundManager.play(plugin, player, "request-sent");

        target.sendMessage(c("&aUse &e/tpaccept &aor &c/tpdeny"));

        return true;
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
