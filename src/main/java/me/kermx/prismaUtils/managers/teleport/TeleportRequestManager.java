package me.kermx.prismaUtils.managers.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportRequestManager {
    private final PrismaUtils plugin;
    private final Map<UUID, TeleportRequest> requests = new HashMap<>();

    public TeleportRequestManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public boolean hasActiveRequest(UUID target) {
        return requests.containsKey(target);
    }

    public TeleportRequest getRequest(UUID target) {
        return requests.get(target);
    }

    public void addRequest(TeleportRequest request) {
        // Cancel any existing request
        if (requests.containsKey(request.getTarget())) {
            cancelRequest(request.getTarget());
        }

        // Store new request
        requests.put(request.getTarget(), request);

        // Set up expiry task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // If request still exists when this runs
            if (requests.containsKey(request.getTarget()) &&
                    requests.get(request.getTarget()).equals(request)) {

                // Send expiry messages
                Player sender = Bukkit.getPlayer(request.getSender());
                if (sender != null && sender.isOnline()) {
                    sender.sendMessage(TextUtils.deserializeString(
                            "<red>Your teleport request to <white>" +
                                    request.getTargetName() + "<red> has expired."
                    ));
                }

                Player target = Bukkit.getPlayer(request.getTarget());
                if (target != null && target.isOnline()) {
                    target.sendMessage(TextUtils.deserializeString(
                            "<red>Teleport request from <white>" +
                                    request.getSenderName() + "<red> has expired."
                    ));
                }

                // Remove the request
                requests.remove(request.getTarget());
            }
        }, 20 * 30); // 30 seconds

        request.setExpiryTask(task);
    }

    public void cancelRequest(UUID target) {
        if (requests.containsKey(target)) {
            TeleportRequest request = requests.get(target);
            if (request.getExpiryTask() != null) {
                request.getExpiryTask().cancel();
            }
            requests.remove(target);
        }
    }

    /**
     * Sends a teleport request with clickable accept/deny buttons
     */
    public void sendRequest(TeleportRequest request) {
        Player target = Bukkit.getPlayer(request.getTarget());
        if (target == null || !target.isOnline()) {
            return;
        }

        // Create the request message with clickable elements
        String message;
        if (request.getType() == TeleportRequest.Type.TPA) {
            message = "<yellow>" + request.getSenderName() + " <white>has requested to teleport to you.";
        } else {
            message = "<yellow>" + request.getSenderName() + " <white>has requested that you teleport to them.";
        }

        target.sendMessage(TextUtils.deserializeString(message));

        // Create clickable accept button
        Component acceptButton = Component.text("[Accept]")
                .clickEvent(ClickEvent.runCommand("/tpaccept"))
                .hoverEvent(HoverEvent.showText(TextUtils.deserializeString(
                        "<green>Click to accept the teleport request"
                )));

        // Create clickable deny button
        Component denyButton = Component.text("[Deny]")
                .clickEvent(ClickEvent.runCommand("/tpdeny"))
                .hoverEvent(HoverEvent.showText(TextUtils.deserializeString(
                        "<red>Click to deny the teleport request"
                )));

        // Combine components
        Component buttons = Component.empty()
                .append(TextUtils.deserializeString("<green>"))
                .append(acceptButton)
                .append(TextUtils.deserializeString(" <red>"))
                .append(denyButton);

        // Send clickable buttons
        target.sendMessage(buttons);

        // Add info on expiry
        target.sendMessage(TextUtils.deserializeString(
                "<gray>This request will expire in 30 seconds."
        ));

        // Add request to manager (which handles expiry)
        addRequest(request);
    }
}
