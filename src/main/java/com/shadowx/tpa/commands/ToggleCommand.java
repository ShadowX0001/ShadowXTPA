package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RequestManager requestManager;

    public ToggleCommand(JavaPlugin plugin, RequestManager requestManager) {
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

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("shadowxtpa.toggle")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        String path = "disabled-requests." + player.getUniqueId();

        boolean defaultEnabled = plugin.getConfig()
                .getBoolean("settings.request-toggle-default", true);

        boolean currentlyDisabled = plugin.getConfig()
                .getBoolean(path, !defaultEnabled);

        boolean newDisabled = !currentlyDisabled;

        plugin.getConfig().set(path, newDisabled);
        plugin.saveConfig();

        if (newDisabled) {
            player.sendMessage("§cYou will no longer receive TPA requests.");
            requestManager.addHistory(
                    player.getUniqueId(),
                    "Disabled incoming TPA requests"
            );
        } else {
            player.sendMessage("§aYou can now receive TPA requests.");
            requestManager.addHistory(
                    player.getUniqueId(),
                    "Enabled incoming TPA requests"
            );
        }

        return true;
    }
}
