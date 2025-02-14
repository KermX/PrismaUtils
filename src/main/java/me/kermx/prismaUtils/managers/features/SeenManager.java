package me.kermx.prismaUtils.managers.features;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeenManager {

    private final Map<UUID, Long> loginTimes = new HashMap<>();

    public void recordLogin(UUID uuid, long loginTime) {
        loginTimes.put(uuid, loginTime);
    }

    public void clearLogin(UUID uuid) {
        loginTimes.remove(uuid);
    }

    public Long getLoginTime(UUID uuid) {
        return loginTimes.get(uuid);
    }
}
