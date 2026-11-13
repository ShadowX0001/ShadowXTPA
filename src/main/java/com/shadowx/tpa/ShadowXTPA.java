package com.shadowx.tpa;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShadowXTPA extends JavaPlugin {

    private static ShadowXTPA instance;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        getLogger().info("================================");
        getLogger().info("       ShadowXTPA Enabled");
        getLogger().info("       Version: 1.0.0");
        getLogger().info("       Advanced TPA System");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {

        getLogger().info("ShadowXTPA Disabled!");

        instance = null;
    }

    public static ShadowXTPA getInstance() {
        return instance;
    }
}
