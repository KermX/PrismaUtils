package me.kermx.prismaUtils.managers.PlayerData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerData {
    private final UUID playerID;
    private boolean flyEnabled;
    private boolean godEnabled;
    private LocalDateTime firstJoin;
    private List<MailMessage> mailbox;

    // Use CopyOnWriteArrayList for thread-safe iteration without explicit synchronization
    private final List<PlayerDataChangeListener> changeListeners = new CopyOnWriteArrayList<>();

    private PlayerData(Builder builder) {
        this.playerID = builder.playerID;
        this.flyEnabled = builder.flyEnabled;
        this.godEnabled = builder.godEnabled;
        this.firstJoin = builder.firstJoin;
        this.mailbox = builder.mailbox;
    }

    /**
     * Add a listener to be notified of data changes.
     * @param listener The listener to add
     */
    public void addChangeListener(PlayerDataChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    /**
     * Remove a previously registered listener.
     * @param listener The listener to remove
     */
    public void removeChangeListener(PlayerDataChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Notify all registered listeners about a data change.
     * @param field The name of the field that changed
     * @param newValue The new value of the field
     */
    private void notifyListeners(String field, Object newValue) {
        for (PlayerDataChangeListener listener : changeListeners) {
            try {
                listener.onDataChanged(playerID, field, newValue);
            } catch (Exception e) {
                // Prevent one bad listener from breaking others
                System.err.println("Error in PlayerDataChangeListener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Getters remain the same
    public UUID getPlayerID() {
        return playerID;
    }

    public boolean isFlyEnabled() {
        return flyEnabled;
    }

    // Update setters to notify listeners
    public void setFlyEnabled(boolean flyEnabled) {
        if (this.flyEnabled != flyEnabled) {
            this.flyEnabled = flyEnabled;
            notifyListeners("flyEnabled", flyEnabled);
        }
    }

    public boolean isGodEnabled() {
        return godEnabled;
    }

    public void setGodEnabled(boolean godEnabled) {
        if (this.godEnabled != godEnabled) {
            this.godEnabled = godEnabled;
            notifyListeners("godMode", godEnabled);
        }
    }

    public LocalDateTime getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(LocalDateTime firstJoin) {
        if (this.firstJoin != firstJoin) {
            this.firstJoin = firstJoin;
            notifyListeners("firstJoin", firstJoin);
        }
    }

    public List<MailMessage> getMailbox() {
        return mailbox;
    }

    public void addMailMessage(MailMessage message) {
        this.mailbox.add(message);
        notifyListeners("mailbox.add", message);
    }

    public void clearMailbox() {
        if (!this.mailbox.isEmpty()) {
            this.mailbox.clear();
            notifyListeners("mailbox.clear", null);
        }
    }

    // Builder class stays the same
    public static class Builder {
        private final UUID playerID;
        private boolean flyEnabled = false;
        private boolean godEnabled = false;
        private LocalDateTime firstJoin = LocalDateTime.now();
        private List<MailMessage> mailbox = new ArrayList<>();

        public Builder(UUID playerID) {
            this.playerID = playerID;
        }

        public Builder flyEnabled(boolean flyEnabled) {
            this.flyEnabled = flyEnabled;
            return this;
        }

        public Builder godEnabled(boolean godEnabled) {
            this.godEnabled = godEnabled;
            return this;
        }

        public Builder firstJoin(LocalDateTime firstJoin) {
            this.firstJoin = firstJoin;
            return this;
        }

        public Builder mailbox(List<MailMessage> mailbox) {
            this.mailbox = mailbox;
            return this;
        }

        public PlayerData build() {
            return new PlayerData(this);
        }
    }
}