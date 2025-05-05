package me.kermx.prismaUtils.managers.teleport;

import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.UUID;

public class TeleportRequest {

    public enum Type {
        TPA,
        TPAHERE
    }

    private final UUID sender;
    private final String senderName;
    private final UUID target;
    private final String targetName;
    private final Type type;
    private BukkitTask expiryTask;

    public TeleportRequest(UUID sender, String senderName, UUID target, String targetName, Type type) {
        this.sender = sender;
        this.senderName = senderName;
        this.target = target;
        this.targetName = targetName;
        this.type = type;
    }

    public UUID getSender() {
        return sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public UUID getTarget() {
        return target;
    }

    public String getTargetName() {
        return targetName;
    }

    public Type getType() {
        return type;
    }

    public BukkitTask getExpiryTask() {
        return expiryTask;
    }

    public void setExpiryTask(BukkitTask expiryTask) {
        this.expiryTask = expiryTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleportRequest that = (TeleportRequest) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(target, that.target) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, target, type);
    }
}

