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

        int delay = plugin.getConfig()
                .getInt("settings.teleport-delay", 5);

        if (player.hasPermission("shadowxtpa.bypass.delay")) {
            delay = 0;
        }

        if (delay <= 0) {
            performTeleport(player, target);
            return;
        }

        UUID id = player.getUniqueId();

        origins.put(id, player.getLocation().clone());

        player.sendMessage("§aTeleporting in §e" + delay + " seconds!");

        SoundManager.play(plugin, player, "teleport-countdown");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    tasks.remove(id);
                    origins.remove(id);

                    if (!player.isOnline() || !target.isOnline()) {
                        return;
                    }

                    performTeleport(player, target);

                },
                delay * 20L
        );

        tasks.put(id, task);
    }

    private void performTeleport(Player player, Player target) {

        if (!player.isOnline() || !target.isOnline()) return;

        boolean crossWorld = plugin.getConfig()
                .getBoolean("settings.allow-cross-world", true);

        if (!crossWorld &&
                !player.getWorld().equals(target.getWorld())) {

            player.sendMessage("§cCross-world teleport is disabled!");
            return;
        }

        Location destination = target.getLocation();

        boolean success = player.teleport(destination);

        if (success) {

            player.sendMessage("§aTeleported to §e" + target.getName());

            target.sendMessage("§e" + player.getName()
                    + " §ahas teleported to you!");

            SoundManager.play(plugin, player, "teleport-success");
            SoundManager.play(plugin, target, "teleport-success");
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

        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }

        tasks.clear();
        origins.clear();
    }

    public void cancelOnMove(Player player) {

        if (!plugin.getConfig().getBoolean(
                "settings.cancel-on-move", true)) return;

        Location origin = origins.get(player.getUniqueId());

        if (origin == null) return;

        Location current = player.getLocation();

        if (origin.getBlockX() != current.getBlockX()
                || origin.getBlockY() != current.getBlockY()
                || origin.getBlockZ() != current.getBlockZ()) {

            cancel(player, true);
        }
    }
}
