package com.shadowx.tpa.commands;

import com.shadowx.tpa.managers.RequestManager;
import com.shadowx.tpa.managers.RequestManager.TPARequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.Collection;

public class ListCommand implements CommandExecutor {
    private final RequestManager requestManager;

    public ListCommand(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("shadowxtpa.use")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }

        Collection<TPARequest> requests = requestManager.getRequests(player.getUniqueId());
        if (requests.isEmpty()) {
            player.sendMessage(Component.text("You don't have any pending TPA requests.", NamedTextColor.YELLOW));
            return true;
        }

        player.sendMessage(Component.text("===== Pending TPA Requests =====", NamedTextColor.GOLD));
        for (TPARequest request : requests) {
            Player requester = Bukkit.getPlayer(request.getSender());
            if (requester == null) continue;

            player.sendMessage(Component.text(requester.getName() + " | " + request.getType().name() + "  ", NamedTextColor.AQUA)
                    .append(Component.text("[ACCEPT]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/tpaccept " + requester.getName()))
                            .hoverEvent(HoverEvent.showText(Component.text("Accept request"))))
                    .append(Component.text(" "))
                    .append(Component.text("[DENY]", NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/tpdeny " + requester.getName()))
                            .hoverEvent(HoverEvent.showText(Component.text("Deny request")))));
        }
        return true;
    }
}
