package com.shadowx.tpa;

import com.shadowx.tpa.commands.*;
import com.shadowx.tpa.listeners.TeleportListener;
import com.shadowx.tpa.managers.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShadowXTPA extends JavaPlugin {
    private static ShadowXTPA instance;
    private RequestManager requestManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        requestManager = new RequestManager(this);
        teleportManager = new TeleportManager(this);

        register("tpa", new TPACommand(this, requestManager));
        register("tpahere", new TPACommand(this, requestManager));
        register("tpaccept", new AcceptCommand(this, requestManager, teleportManager));
        register("tpdeny", new DenyCommand(this, requestManager));
        register("tpacancel", new CancelCommand(this, requestManager));
        register("tpignore", new IgnoreCommand(this, requestManager));
        register("tpignorelist", new IgnoreListCommand(this));
        register("tpatoggle", new ToggleCommand(this, requestManager));
        register("tpahistory", new HistoryCommand(this));
        register("tpalist", new ListCommand(requestManager));
        register("tpareload", new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(
                new TeleportListener(this, teleportManager, requestManager), this);

        getLogger().info("ShadowXTPA enabled successfully!");
    }

    private void register(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Missing command in plugin.yml: " + name);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(executor);
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) teleportManager.cancelAll();
        if (requestManager != null) requestManager.clear();
        instance = null;
        getLogger().info("ShadowXTPA disabled!");
    }

    public static ShadowXTPA getInstance() { return instance; }
    public RequestManager getRequestManager() { return requestManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
}
