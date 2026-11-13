package com.shadowx.tpa;

import com.shadowx.tpa.commands.TPACommand;
import com.shadowx.tpa.managers.RequestManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShadowXTPA extends JavaPlugin {

    private static ShadowXTPA instance;

    private RequestManager requestManager;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        // Initialize Request Manager
        requestManager = new RequestManager(this);

        // Register TPA command
        registerCommand("tpa", new TPACommand(this, requestManager));

        // Register TPAHere command
        registerCommand("tpahere", new TPACommand(this, requestManager));

        getLogger().info("================================");
        getLogger().info("       ShadowXTPA Enabled");
        getLogger().info("       Version: 1.0.0");
        getLogger().info("       Advanced TPA System");
        getLogger().info("================================");
    }

    private void registerCommand(String name, TPACommand executor) {

        PluginCommand command = getCommand(name);

        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().severe("Command not found: " + name);
        }
    }

    @Override
    public void onDisable() {

        if (requestManager != null) {
            requestManager.clear();
        }

        getLogger().info("ShadowXTPA Disabled!");

        instance = null;
    }

    public static ShadowXTPA getInstance() {
        return instance;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }
}
