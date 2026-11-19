package com.shadowx.tpa;

import com.shadowx.tpa.commands.*;
import com.shadowx.tpa.listeners.TeleportListener;
import com.shadowx.tpa.managers.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public final class ShadowXTPA extends JavaPlugin {
    private static ShadowXTPA instance;
    private RequestManager requestManager;
    private TeleportManager teleportManager;
    private YamlConfiguration messages;
    private YamlConfiguration sounds;
    private YamlConfiguration history;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("sounds.yml", false);
        saveResource("history.yml", false);
        reloadDataFiles();

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
        getServer().getPluginManager().registerEvents(new TeleportListener(this, teleportManager, requestManager), this);
        getLogger().info("ShadowXTPA enabled successfully!");
    }

    public void reloadDataFiles() {
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        sounds = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "sounds.yml"));
        history = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "history.yml"));
    }

    public String message(String key, String fallback) {
        return messages == null ? fallback : messages.getString("messages." + key, fallback);
    }
    public YamlConfiguration getMessages() { return messages; }
    public YamlConfiguration getSounds() { return sounds; }
    public YamlConfiguration getHistory() { return history; }
    public void saveHistory() {
        try { history.save(new File(getDataFolder(), "history.yml")); }
        catch (java.io.IOException ex) { getLogger().severe("Could not save history.yml: " + ex.getMessage()); }
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
    }
    public static ShadowXTPA getInstance() { return instance; }
    public RequestManager getRequestManager() { return requestManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
}