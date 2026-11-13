package com.shadowx.tpa.listeners;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportListener implements Listener {
    private final JavaPlugin plugin;
    private final TeleportManager teleports;
    private final RequestManager requests;

    public TeleportListener(JavaPlugin plugin, TeleportManager teleports, RequestManager requests) {
        this.plugin = plugin;
        this.teleports = teleports;
        this.requests = requests;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() != null) {
            teleports.cancelOnMove(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getFinalDamage() > 0
                && plugin.getConfig().getBoolean("settings.cancel-on-damage", true)) {
            teleports.cancel(player, true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("settings.cancel-on-death", true)) {
            teleports.cancel(event.getEntity(), true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (plugin.getConfig().getBoolean("settings.cancel-on-world-change", true)) {
            teleports.cancel(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        teleports.cancel(player, false);
        requests.removePlayerRequests(player.getUniqueId());
    }
}
