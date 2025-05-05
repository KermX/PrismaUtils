package me.kermx.prismaUtils.managers.PlayerData;

import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerData {
    private final UUID playerID;
    private boolean flyEnabled;
    private boolean godEnabled;
    private LocalDateTime firstJoin;
    private List<MailMessage> mailbox;
    private Map<String, Home> homes;
    private Location lastLocation;

    // Use CopyOnWriteArrayList for thread-safe iteration without explicit synchronization
    private final List<PlayerDataChangeListener> changeListeners = new CopyOnWriteArrayList<>();

    private PlayerData(Builder builder) {
        this.playerID = builder.playerID;
        this.flyEnabled = builder.flyEnabled;
        this.godEnabled = builder.godEnabled;
        this.firstJoin = builder.firstJoin;
        this.mailbox = builder.mailbox;
        this.homes = builder.homes;
        this.lastLocation = builder.lastLocation;
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

    public Map<String, Home> getHomes() {
        return homes;
    }

    public Home getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public void addHome(String name, Home home) {
        this.homes.put(name.toLowerCase(), home);
        notifyListeners("homes.add", home);
    }

    public boolean removeHome(String name) {
        Home removed = this.homes.remove(name.toLowerCase());
        if (removed != null) {
            notifyListeners("homes.remove", name);
            return true;
        }
        return false;
    }

    public int getHomesCount() {
        return homes.size();
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        if (this.lastLocation != lastLocation) {
            this.lastLocation = lastLocation;
            notifyListeners("lastLocation", lastLocation);
        }
    }

    // Builder class stays the same
    public static class Builder {
        private final UUID playerID;
        private boolean flyEnabled = false;
        private boolean godEnabled = false;
        private LocalDateTime firstJoin = LocalDateTime.now();
        private List<MailMessage> mailbox = new ArrayList<>();
        private Map<String, Home> homes = new HashMap<>();
        private Location lastLocation;

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

        public Builder homes(Map<String, Home> homes) {
            this.homes = homes;
            return this;
        }

        public Builder lastLocation(Location lastLocation) {
            this.lastLocation = lastLocation;
            return this;
        }

        public PlayerData build() {
            return new PlayerData(this);
        }
    }
}