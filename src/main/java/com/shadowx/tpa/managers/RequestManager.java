package com.shadowx.tpa.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RequestManager {

    public enum RequestType { TPA, TPAHERE }

    public static class TPARequest {
        private final UUID sender;
        private final UUID receiver;
        private final RequestType type;
        private final long createdAt;
        private BukkitTask expiryTask;

        public TPARequest(UUID sender, UUID receiver, RequestType type) {
            this.sender = sender;
            this.receiver = receiver;
            this.type = type;
            this.createdAt = System.currentTimeMillis();
        }

        public UUID getSender() { return sender; }
        public UUID getReceiver() { return receiver; }
        public RequestType getType() { return type; }
        public long getCreatedAt() { return createdAt; }
        public void setExpiryTask(BukkitTask task) { this.expiryTask = task; }
        public void cancelExpiry() {
            if (expiryTask != null) {
                expiryTask.cancel();
                expiryTask = null;
            }
        }
    }

    private final JavaPlugin plugin;
    private final Map<UUID, Map<UUID, TPARequest>> requests = new HashMap<>();

    public RequestManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasRequest(UUID sender, UUID receiver) {
        Map<UUID, TPARequest> incoming = requests.get(receiver);
        return incoming != null && incoming.containsKey(sender);
    }

    public int getPendingCount(UUID receiver) {
        return requests.getOrDefault(receiver, Collections.emptyMap()).size();
    }

    public long getCooldownRemaining(UUID playerId) {
        long cooldownSeconds = Math.max(0L, plugin.getConfig().getLong("settings.cooldown", 30));
        long lastUsed = plugin.getConfig().getLong("cooldowns." + playerId, 0L);
        long remainingMillis = lastUsed + cooldownSeconds * 1000L - System.currentTimeMillis();
        return Math.max(0L, (remainingMillis + 999L) / 1000L);
    }

    public boolean isIgnored(UUID receiver, UUID sender) {
        if (!plugin.getConfig().getBoolean("settings.ignore-system", true)) return false;
        return plugin.getConfig().getStringList("ignored." + receiver).contains(sender.toString());
    }

    public boolean isRequestsEnabled(UUID player) {
        boolean defaultEnabled = plugin.getConfig().getBoolean("settings.request-toggle-default", true);
        boolean disabled = plugin.getConfig().getBoolean("disabled-requests." + player, !defaultEnabled);
        return !disabled;
    }

    public boolean addRequest(Player sender, Player receiver, RequestType type, int timeout) {
        UUID senderId = sender.getUniqueId();
        UUID receiverId = receiver.getUniqueId();

        if (senderId.equals(receiverId) || !receiver.isOnline()) return false;
        if (!isRequestsEnabled(receiverId) || isIgnored(receiverId, senderId)) return false;

        if (!sender.hasPermission("shadowxtpa.bypass.cooldown") && getCooldownRemaining(senderId) > 0) {
            return false;
        }

        boolean preventDuplicates = plugin.getConfig().getBoolean("settings.prevent-duplicate-requests", true);
        if (hasRequest(senderId, receiverId)) {
            if (preventDuplicates) return false;
            removeRequest(senderId, receiverId);
        }

        int maximum = Math.max(1, plugin.getConfig().getInt("settings.max-pending-requests", 5));
        if (getPendingCount(receiverId) >= maximum) return false;

        TPARequest request = new TPARequest(senderId, receiverId, type);
        requests.computeIfAbsent(receiverId, key -> new HashMap<>()).put(senderId, request);

        long safeTimeout = Math.max(1, timeout);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!removeRequest(senderId, receiverId)) return;

            Player onlineSender = Bukkit.getPlayer(senderId);
            Player onlineReceiver = Bukkit.getPlayer(receiverId);

            if (onlineSender != null) {
                onlineSender.sendMessage("§cYour TPA request to " +
                        (onlineReceiver != null ? onlineReceiver.getName() : "that player") + " expired.");
                SoundManager.play(plugin, onlineSender, "request-cancelled");
            }
            if (onlineReceiver != null) {
                onlineReceiver.sendMessage("§cThe TPA request from " +
                        (onlineSender != null ? onlineSender.getName() : "a player") + " expired.");
                SoundManager.play(plugin, onlineReceiver, "request-cancelled");
            }
            addHistory(senderId, "TPA request to " + receiver.getName() + " expired");
            addHistory(receiverId, "TPA request from " + sender.getName() + " expired");
        }, safeTimeout * 20L);
        request.setExpiryTask(task);

        if (!sender.hasPermission("shadowxtpa.bypass.cooldown")) {
            plugin.getConfig().set("cooldowns." + senderId, System.currentTimeMillis());
            plugin.saveConfig();
        }

        addHistory(senderId, "Sent " + type + " request to " + receiver.getName());
        addHistory(receiverId, "Received " + type + " request from " + sender.getName());
        return true;
    }

    public TPARequest getRequest(UUID sender, UUID receiver) {
        Map<UUID, TPARequest> incoming = requests.get(receiver);
        return incoming == null ? null : incoming.get(sender);
    }

    public Collection<TPARequest> getRequests(UUID receiver) {
        Map<UUID, TPARequest> incoming = requests.get(receiver);
        if (incoming == null) return Collections.emptyList();
        return Collections.unmodifiableCollection(new ArrayList<>(incoming.values()));
    }

    public boolean removeRequest(UUID sender, UUID receiver) {
        Map<UUID, TPARequest> incoming = requests.get(receiver);
        if (incoming == null) return false;

        TPARequest removed = incoming.remove(sender);
        if (removed == null) return false;

        removed.cancelExpiry();
        if (incoming.isEmpty()) requests.remove(receiver);
        return true;
    }

    public void addHistory(UUID player, String message) {
        String path = "history." + player;
        List<String> history = new ArrayList<>(plugin.getConfig().getStringList(path));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        history.add(timestamp + " - " + message);

        int maximum = Math.max(1, plugin.getConfig().getInt("settings.history-limit", 20));
        while (history.size() > maximum) history.remove(0);

        plugin.getConfig().set(path, history);
        plugin.saveConfig();
    }

    public void removePlayerRequests(UUID playerId) {
        Map<UUID, TPARequest> incoming = requests.remove(playerId);
        if (incoming != null) incoming.values().forEach(TPARequest::cancelExpiry);

        Iterator<Map.Entry<UUID, Map<UUID, TPARequest>>> iterator = requests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map<UUID, TPARequest> pending = iterator.next().getValue();
            TPARequest removed = pending.remove(playerId);
            if (removed != null) removed.cancelExpiry();
            if (pending.isEmpty()) iterator.remove();
        }

        // Deliberately retain cooldowns in config: reconnecting must not bypass cooldown.
    }

    public void clear() {
        for (Map<UUID, TPARequest> incoming : requests.values()) {
            incoming.values().forEach(TPARequest::cancelExpiry);
        }
        requests.clear();
    }
}
