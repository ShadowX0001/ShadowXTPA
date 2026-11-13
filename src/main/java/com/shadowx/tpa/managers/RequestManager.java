package com.shadowx.tpa.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.*;

public class RequestManager {

    public enum RequestType {
        TPA,
        TPAHERE
    }

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

        public UUID getSender() {
            return sender;
        }

        public UUID getReceiver() {
            return receiver;
        }

        public RequestType getType() {
            return type;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setExpiryTask(BukkitTask task) {
            this.expiryTask = task;
        }

        public void cancelExpiry() {
            if (expiryTask != null) {
                expiryTask.cancel();
            }
        }
    }

    private final JavaPlugin plugin;

    private final Map<UUID, Map<UUID, TPARequest>> requests =
            new HashMap<>();

    private final Map<UUID, Long> cooldowns =
            new HashMap<>();

    public RequestManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Check existing request

    public boolean hasRequest(UUID sender, UUID receiver) {
        return requests.containsKey(receiver)
                && requests.get(receiver).containsKey(sender);
    }

    // Pending request count

    public int getPendingCount(UUID receiver) {
        return requests.getOrDefault(
                receiver,
                Collections.emptyMap()
        ).size();
    }

    // Cooldown system

    public long getCooldownRemaining(UUID player) {

        long cooldown = plugin.getConfig()
                .getLong("settings.cooldown", 30);

        long lastUsed = cooldowns.getOrDefault(player, 0L);

        long remaining = (lastUsed + cooldown * 1000L)
                - System.currentTimeMillis();

        return Math.max(0, (remaining + 999) / 1000);
    }

    // Ignore system

    public boolean isIgnored(UUID receiver, UUID sender) {

        if (!plugin.getConfig().getBoolean(
                "settings.ignore-system", true)) {
            return false;
        }

        List<String> ignored = plugin.getConfig()
                .getStringList("ignored." + receiver);

        return ignored.contains(sender.toString());
    }

    // Request toggle system

    public boolean isRequestsEnabled(UUID player) {

        boolean defaultEnabled = plugin.getConfig()
                .getBoolean("settings.request-toggle-default", true);

        boolean disabled = plugin.getConfig()
                .getBoolean("disabled-requests." + player,
                        !defaultEnabled);

        return !disabled;
    }

    // Add request

    public boolean addRequest(
            Player sender,
            Player receiver,
            RequestType type,
            int timeout
    ) {

        UUID senderId = sender.getUniqueId();
        UUID receiverId = receiver.getUniqueId();

        if (senderId.equals(receiverId)) {
            return false;
        }

        // Check target toggle

        if (!isRequestsEnabled(receiverId)) {
            return false;
        }

        // Check ignore system

        if (isIgnored(receiverId, senderId)) {
            return false;
        }

        // Check cooldown

        if (!sender.hasPermission("shadowxtpa.bypass.cooldown")
                && getCooldownRemaining(senderId) > 0) {
            return false;
        }

        // Duplicate protection

        boolean preventDuplicates = plugin.getConfig()
                .getBoolean("settings.prevent-duplicate-requests", true);

        if (preventDuplicates && hasRequest(senderId, receiverId)) {
            return false;
        }

        // Maximum pending requests

        int maximum = plugin.getConfig()
                .getInt("settings.max-pending-requests", 5);

        if (getPendingCount(receiverId) >= maximum) {
            return false;
        }

        // Replace old request if duplicates are allowed

        if (hasRequest(senderId, receiverId)) {
            removeRequest(senderId, receiverId);
        }

        TPARequest request = new TPARequest(
                senderId,
                receiverId,
                type
        );

        requests.computeIfAbsent(
                receiverId,
                key -> new HashMap<>()
        ).put(senderId, request);

        // Start expiry timer

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> removeRequest(senderId, receiverId),
                timeout * 20L
        );

        request.setExpiryTask(task);

        // Start cooldown

        cooldowns.put(senderId, System.currentTimeMillis());

        // Record history

        addHistory(senderId,
                "Sent " + type + " request to " + receiver.getName());

        addHistory(receiverId,
                "Received " + type + " request from " + sender.getName());

        return true;
    }

    // Get specific request

    public TPARequest getRequest(UUID sender, UUID receiver) {

        Map<UUID, TPARequest> receiverRequests =
                requests.get(receiver);

        if (receiverRequests == null) {
            return null;
        }

        return receiverRequests.get(sender);
    }

    // Get incoming requests

    public Collection<TPARequest> getRequests(UUID receiver) {

        return Collections.unmodifiableCollection(
                requests.getOrDefault(
                        receiver,
                        Collections.emptyMap()
                ).values()
        );
    }

    // Remove request

    public boolean removeRequest(UUID sender, UUID receiver) {

        Map<UUID, TPARequest> receiverRequests =
                requests.get(receiver);

        if (receiverRequests == null) {
            return false;
        }

        TPARequest request = receiverRequests.remove(sender);

        if (request == null) {
            return false;
        }

        request.cancelExpiry();

        if (receiverRequests.isEmpty()) {
            requests.remove(receiver);
        }

        return true;
    }

    // History system

    public void addHistory(UUID player, String message) {

        String path = "history." + player;

        List<String> history = new ArrayList<>(
                plugin.getConfig().getStringList(path)
        );

        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date());

        history.add(time + " - " + message);

        int maximum = plugin.getConfig()
                .getInt("settings.history-limit", 20);

        while (history.size() > maximum) {
            history.remove(0);
        }

        plugin.getConfig().set(path, history);
        plugin.saveConfig();
    }

    // Remove player's requests

    public void removePlayerRequests(UUID playerId) {

        Map<UUID, TPARequest> incoming =
                requests.remove(playerId);

        if (incoming != null) {
            incoming.values().forEach(TPARequest::cancelExpiry);
        }

        Iterator<Map.Entry<UUID, Map<UUID, TPARequest>>> iterator =
                requests.entrySet().iterator();

        while (iterator.hasNext()) {

            Map<UUID, TPARequest> playerRequests =
                    iterator.next().getValue();

            TPARequest request = playerRequests.remove(playerId);

            if (request != null) {
                request.cancelExpiry();
            }

            if (playerRequests.isEmpty()) {
                iterator.remove();
            }
        }

        cooldowns.remove(playerId);
    }

    // Clear all requests

    public void clear() {

        for (Map<UUID, TPARequest> playerRequests : requests.values()) {
            playerRequests.values().forEach(TPARequest::cancelExpiry);
        }

        requests.clear();
        cooldowns.clear();
    }
}
