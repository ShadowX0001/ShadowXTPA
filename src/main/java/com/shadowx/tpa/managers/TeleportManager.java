package com.shadowx.tpa.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Map<UUID, Location> origins = new HashMap<>();

    public TeleportManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, Player target) {
        if (!player.isOnline() || !target.isOnline()) return;
        cancel(player, false);

        int delay = Math.max(0, plugin.getConfig().getInt("settings.teleport-delay", 5));
        if (player.hasPermission("shadowxtpa.bypass.delay")) delay = 0;

        if (!plugin.getConfig().getBoolean("settings.allow-cross-world", true)
                && !player.getWorld().equals(target.getWorld())) {
            player.sendMessage("§cCross-world teleport is disabled!");
            return;
        }

        if (delay == 0) {
            performTeleport(player, target);
            return;
        }

        UUID playerId = player.getUniqueId();
        origins.put(playerId, player.getLocation().clone());
        player.sendMessage("§aTeleporting in §e" + delay + " seconds§a. Don't move!");
        SoundManager.play(plugin, player, "teleport-countdown");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tasks.remove(playerId);
            origins.remove(playerId);

            Player onlinePlayer = Bukkit.getPlayer(playerId);
            Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());

            if (onlinePlayer == null) return;
            if (onlineTarget == null) {
                onlinePlayer.sendMessage("§cTeleport cancelled: target went offline.");
                SoundManager.play(plugin, onlinePlayer, "teleport-cancel");
                return;
            }
            performTeleport(onlinePlayer, onlineTarget);
        }, delay * 20L);

        tasks.put(playerId, task);
    }

    private void performTeleport(Player player, Player target) {
        if (!player.isOnline() || !target.isOnline()) return;

        if (!plugin.getConfig().getBoolean("settings.allow-cross-world", true)
                && !player.getWorld().equals(target.getWorld())) {
            player.sendMessage("§cCross-world teleport is disabled!");
            return;
        }

        boolean success = player.teleport(target.getLocation());
        if (success) {
            player.sendMessage("§aTeleported to §e" + target.getName() + "§a!");
            target.sendMessage("§e" + player.getName() + " §ahas teleported to you!");
            SoundManager.play(plugin, player, "teleport-success");
            SoundManager.play(plugin, target, "teleport-success");
        } else {
            player.sendMessage("§cTeleport failed. Please try again.");
            SoundManager.play(plugin, player, "teleport-cancel");
        }
    }

    public boolean isTeleporting(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    public void cancel(Player player, boolean notify) {
        UUID id = player.getUniqueId();
        BukkitTask task = tasks.remove(id);
        if (task == null) return;

        task.cancel();
        origins.remove(id);

        if (notify && player.isOnline()) {
            player.sendMessage("§cTeleport cancelled!");
            SoundManager.play(plugin, player, "teleport-cancel");
        }
    }

    public void cancelAll() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
        origins.clear();
    }

    public void cancelOnMove(Player player, Location destination) {
        if (!plugin.getConfig().getBoolean("settings.cancel-on-move", true)) return;
        Location origin = origins.get(player.getUniqueId());
        if (origin == null || destination == null) return;

        // Ignore head rotation; cancel only when the player's feet position changes.
        if (origin.getWorld() != destination.getWorld()
                || origin.getX() != destination.getX()
                || origin.getY() != destination.getY()
                || origin.getZ() != destination.getZ()) {
            cancel(player, true);
        }
    }
}
