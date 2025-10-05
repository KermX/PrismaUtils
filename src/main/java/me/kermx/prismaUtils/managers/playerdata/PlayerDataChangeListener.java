package me.kermx.prismaUtils.managers.playerdata;

import java.util.UUID;

public interface PlayerDataChangeListener  {
    /**
     * Called when a field in playerdata changes.
     *
     * @param playerId The UUID of the player whose data changed
     * @param field The name of the field that changed
     * @param newValue The new value of the field
     */
    void onDataChanged(UUID playerId, String field, Object newValue);
}
