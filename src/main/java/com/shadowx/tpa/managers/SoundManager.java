package com.shadowx.tpa.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.shadowx.tpa.ShadowXTPA;

public final class SoundManager {
    public static void play(JavaPlugin plugin, Player player, String soundName) {
        if (player == null || !player.isOnline()) return;
        ShadowXTPA app = (ShadowXTPA) plugin;
        if (!app.getSounds().getBoolean("sounds." + soundName + ".enabled", true)) return;
        String sound = app.getSounds().getString("sounds." + soundName + ".sound", "UI_BUTTON_CLICK");
        float volume = (float) app.getSounds().getDouble("sounds." + soundName + ".volume", 1.0);
        float pitch = (float) app.getSounds().getDouble("sounds." + soundName + ".pitch", 1.0);
        try { player.playSound(player.getLocation(), Sound.valueOf(sound.toUpperCase()), volume, pitch); }
        catch (IllegalArgumentException ex) { plugin.getLogger().warning("Invalid sound '" + sound + "' in sounds.yml"); }
    }
}