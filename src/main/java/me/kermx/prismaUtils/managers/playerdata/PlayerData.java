package me.kermx.prismaUtils.managers.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerData {
    private final UUID playerID;
    private boolean godEnabled;
    private boolean afkEnabled;
    private LocalDateTime firstJoin;
    private List<MailMessage> mailbox;
    private Map<String, Home> homes;
    private String lastLocationWorld;
    private double lastLocationX;
    private double lastLocationY;
    private double lastLocationZ;
    private float lastLocationYaw;
    private float lastLocationPitch;
    private boolean hasLastLocation = false;

    private boolean flightEnabled = false;
    private long tempFlightSeconds = 0;
    private LocalDateTime tempFlightLastUpdated = null;


    // Use CopyOnWriteArrayList for thread-safe iteration without explicit synchronization
    private final List<PlayerDataChangeListener> changeListeners = new CopyOnWriteArrayList<>();

    private PlayerData(Builder builder) {
        this.playerID = builder.playerID;
        this.godEnabled = builder.godEnabled;
        this.afkEnabled = builder.afkEnabled;
        this.firstJoin = builder.firstJoin;
        this.mailbox = new ArrayList<>(builder.mailbox);
        this.homes = new HashMap<>(builder.homes);
        this.flightEnabled = builder.flightEnabled;
        this.tempFlightSeconds = builder.tempFlightSeconds;
        this.tempFlightLastUpdated = builder.tempFlightLastUpdated;

        // Initialize location fields from builder
        this.hasLastLocation = builder.hasLastLocation;
        this.lastLocationWorld = builder.lastLocationWorld;
        this.lastLocationX = builder.lastLocationX;
        this.lastLocationY = builder.lastLocationY;
        this.lastLocationZ = builder.lastLocationZ;
        this.lastLocationYaw = builder.lastLocationYaw;
        this.lastLocationPitch = builder.lastLocationPitch;
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

    public boolean isGodEnabled() {
        return godEnabled;
    }

    public void setGodEnabled(boolean godEnabled) {
        if (this.godEnabled != godEnabled) {
            this.godEnabled = godEnabled;
            notifyListeners("godMode", godEnabled);
        }
    }

    public boolean isAfk() {
        return afkEnabled;
    }

    public void setAfk(boolean afkEnabled) {
        if (this.afkEnabled != afkEnabled) {
            this.afkEnabled = afkEnabled;
            notifyListeners("afkEnabled", afkEnabled);
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
        return Collections.unmodifiableList(mailbox);
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
        return Collections.unmodifiableMap(homes);
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
        if (!hasLastLocation || lastLocationWorld == null) {
            return null;
        }
        World world = Bukkit.getWorld(lastLocationWorld);
        if (world == null) {
            return null;
        }
        return new Location(world, lastLocationX, lastLocationY, lastLocationZ, lastLocationYaw, lastLocationPitch);
    }

    public void setLastLocation(Location location) {
        if (location == null) {
            this.hasLastLocation = false;
            this.lastLocationWorld = null;
            notifyListeners("lastLocation", null);
            return;
        }

        this.hasLastLocation = true;
        this.lastLocationWorld = location.getWorld().getName();
        this.lastLocationX = location.getX();
        this.lastLocationY = location.getY();
        this.lastLocationZ = location.getZ();
        this.lastLocationYaw = location.getYaw();
        this.lastLocationPitch = location.getPitch();

        notifyListeners("lastLocation", location);
    }

    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    public void setFlightEnabled(boolean flightEnabled) {
        if (this.flightEnabled != flightEnabled) {
            this.flightEnabled = flightEnabled;
            notifyListeners("flightEnabled", flightEnabled);
        }
    }

    public long getTempFlightSeconds() {
        return tempFlightSeconds;
    }

    public void setTempFlightSeconds(long tempFlightSeconds) {
        if (this.tempFlightSeconds != tempFlightSeconds) {
            this.tempFlightSeconds = Math.max(0, tempFlightSeconds);
            this.tempFlightLastUpdated = LocalDateTime.now();
            notifyListeners("tempFlightSeconds", this.tempFlightSeconds);
        }
    }

    public void addTempFlightSeconds(long seconds) {
        setTempFlightSeconds(this.tempFlightSeconds + seconds);
    }

    public LocalDateTime getTempFlightLastUpdated() {
        return tempFlightLastUpdated;
    }

    public void setTempFlightLastUpdated(LocalDateTime tempFlightLastUpdated) {
        this.tempFlightLastUpdated = tempFlightLastUpdated;
        notifyListeners("tempFlightLastUpdated", tempFlightLastUpdated);
    }

    public boolean hasTempFlight() {
        return tempFlightSeconds > 0;
    }

    // Builder class
    public static class Builder {
        private final UUID playerID;
        private boolean godEnabled = false;
        private boolean afkEnabled = false;
        private LocalDateTime firstJoin = LocalDateTime.now();
        private List<MailMessage> mailbox = new ArrayList<>();
        private Map<String, Home> homes = new HashMap<>();
        private String lastLocationWorld;
        private double lastLocationX;
        private double lastLocationY;
        private double lastLocationZ;
        private float lastLocationYaw;
        private float lastLocationPitch;
        private boolean hasLastLocation = false;
        private boolean flightEnabled = false;
        private long tempFlightSeconds = 0;
        private LocalDateTime tempFlightLastUpdated = null;



        public Builder(UUID playerID) {
            this.playerID = playerID;
        }

        public Builder godEnabled(boolean godEnabled) {
            this.godEnabled = godEnabled;
            return this;
        }

        public Builder afkEnabled(boolean afkEnabled) {
            this.afkEnabled = afkEnabled;
            return this;
        }

        public Builder firstJoin(LocalDateTime firstJoin) {
            this.firstJoin = firstJoin;
            return this;
        }

        public Builder mailbox(List<MailMessage> mailbox) {
            this.mailbox = mailbox != null ? new ArrayList<>(mailbox) : new ArrayList<>();
            return this;
        }

        public Builder homes(Map<String, Home> homes) {
            this.homes = homes != null ? new HashMap<>(homes) : new HashMap<>();
            return this;
        }

        public Builder lastLocation(Location location) {
            if (location != null) {
                this.hasLastLocation = true;
                this.lastLocationWorld = location.getWorld().getName();
                this.lastLocationX = location.getX();
                this.lastLocationY = location.getY();
                this.lastLocationZ = location.getZ();
                this.lastLocationYaw = location.getYaw();
                this.lastLocationPitch = location.getPitch();
            }
            return this;
        }

        public Builder flightEnabled(boolean flightEnabled) {
            this.flightEnabled = flightEnabled;
            return this;
        }

        public Builder tempFlightSeconds(long tempFlightSeconds) {
            this.tempFlightSeconds = tempFlightSeconds;
            return this;
        }

        public Builder tempFlightLastUpdated(LocalDateTime tempFlightLastUpdated) {
            this.tempFlightLastUpdated = tempFlightLastUpdated;
            return this;
        }

        public PlayerData build() {
            return new PlayerData(this);
        }
    }
}