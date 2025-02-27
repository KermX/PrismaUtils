package me.kermx.prismaUtils.managers.PlayerData;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerData {
    private final UUID playerID;
    private boolean flyEnabled;
    private boolean godEnabled;
    private LocalDateTime firstJoin;

    public PlayerData(UUID playerID) {
        this.playerID = playerID;
        this.flyEnabled = false;
        this.godEnabled = false;
        this.firstJoin = LocalDateTime.now();
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public boolean isFlyEnabled() {
        return flyEnabled;
    }

    public void setFlyEnabled(boolean flyEnabled) {
        this.flyEnabled = flyEnabled;
    }

    public boolean isGodEnabled() {
        return godEnabled;
    }

    public void setGodEnabled(boolean godEnabled) {
        this.godEnabled = godEnabled;
    }

    public LocalDateTime getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(LocalDateTime firstJoin) {
        this.firstJoin = firstJoin;
    }
}
