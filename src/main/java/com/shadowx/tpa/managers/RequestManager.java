package com.shadowx.tpa.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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

    public RequestManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasRequest(UUID sender, UUID receiver) {
        return requests.containsKey(receiver)
                && requests.get(receiver).containsKey(sender);
    }

    public int getPendingCount(UUID receiver) {
        return requests.getOrDefault(receiver, Collections.emptyMap()).size();
    }

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

        if (hasRequest(senderId, receiverId)) {
            return false;
        }

        int maximum = plugin.getConfig()
                .getInt("settings.max-pending-requests", 5);

        if (getPendingCount(receiverId) >= maximum) {
            return false;
        }

        TPARequest request =
                new TPARequest(senderId, receiverId, type);

        requests.computeIfAbsent(
                receiverId,
                key -> new HashMap<>()
        ).put(senderId, request);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> removeRequest(senderId, receiverId),
                timeout * 20L
        );

        request.setExpiryTask(task);

        return true;
    }

    public TPARequest getRequest(UUID sender, UUID receiver) {

        Map<UUID, TPARequest> receiverRequests =
                requests.get(receiver);

        if (receiverRequests == null) {
            return null;
        }

        return receiverRequests.get(sender);
    }

    public Collection<TPARequest> getRequests(UUID receiver) {
        return Collections.unmodifiableCollection(
                requests.getOrDefault(receiver, Collections.emptyMap())
                        .values()
        );
    }

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

    public void removePlayerRequests(UUID playerId) {

        Map<UUID, TPARequest> incoming = requests.remove(playerId);

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
    }

    public void clear() {

        for (Map<UUID, TPARequest> playerRequests : requests.values()) {
            playerRequests.values().forEach(TPARequest::cancelExpiry);
        }

        requests.clear();
    }
          }
