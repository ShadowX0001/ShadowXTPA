package com.shadowx.tpa.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundManager {

    public static void play(
            JavaPlugin plugin,
            Player player,
            String soundName
    ) {

        if (player == null || !player.isOnline()) return;

        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        String sound = plugin.getConfig()
                .getString("sounds." + soundName, "UI_BUTTON_CLICK");

        try {

            Sound selected = Sound.valueOf(sound.toUpperCase());

            float volume = (float) plugin.getConfig()
                    .getDouble("sounds.volume", 1.0);

            float pitch = (float) plugin.getConfig()
                    .getDouble("sounds.pitch", 1.0);

            player.playSound(
                    player.getLocation(),
                    selected,
                    volume,
                    pitch
            );

        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning(
                    "Invalid sound: " + sound
            );
        }
    }
}
