package me.kermx.prismaUtils.managers.PlayerData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private final UUID playerID;
    private boolean flyEnabled;
    private boolean godEnabled;
    private LocalDateTime firstJoin;
    private List<MailMessage> mailbox;


    public PlayerData(UUID playerID) {
        this.playerID = playerID;
        this.flyEnabled = false;
        this.godEnabled = false;
        this.firstJoin = LocalDateTime.now();
        this.mailbox = new ArrayList<>();
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

    public List<MailMessage> getMailbox() {return mailbox;}

    public void addMailMessage(MailMessage message) {
        this.mailbox.add(message);
    }

    public void clearMailbox() {
        this.mailbox.clear();
    }

}
