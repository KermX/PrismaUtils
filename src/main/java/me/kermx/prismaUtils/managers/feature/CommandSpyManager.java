package me.kermx.prismaUtils.managers.feature;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandSpyManager {

    private final Set<UUID> spying = ConcurrentHashMap.newKeySet();

    public boolean toggle(UUID playerId) {
        if (spying.remove(playerId)) {
            return false;
        }
        spying.add(playerId);
        return true;
    }

    public boolean isSpying(UUID playerId) {
        return spying.contains(playerId);
    }

    public Set<UUID> getSpyingPlayers() {
        return spying;
    }
}