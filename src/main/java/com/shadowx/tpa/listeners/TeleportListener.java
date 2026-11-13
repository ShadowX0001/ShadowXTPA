package com.shadowx.tpa.listeners;

import com.shadowx.tpa.managers.TeleportManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportListener implements Listener {

    private final JavaPlugin plugin;
    private final TeleportManager manager;

    public TeleportListener(
            JavaPlugin plugin,
            TeleportManager manager
    ) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (event.getTo() == null) return;

        manager.cancelOnMove(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        if (plugin.getConfig().getBoolean(
                "settings.cancel-on-damage", true)) {

            manager.cancel(player, true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (plugin.getConfig().getBoolean(
                "settings.cancel-on-death", true)) {

            manager.cancel(event.getEntity(), true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        if (plugin.getConfig().getBoolean(
                "settings.cancel-on-world-change", true)) {

            manager.cancel(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.cancel(event.getPlayer(), false);
    }
}
