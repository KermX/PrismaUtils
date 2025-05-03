package me.kermx.prismaUtils.managers.PlayerData;

import java.util.UUID;

public interface PlayerDataChangeListener  {
    /**
     * Called when a field in PlayerData changes.
     *
     * @param playerId The UUID of the player whose data changed
     * @param field The name of the field that changed
     * @param newValue The new value of the field
     */
    void onDataChanged(UUID playerId, String field, Object newValue);
}
