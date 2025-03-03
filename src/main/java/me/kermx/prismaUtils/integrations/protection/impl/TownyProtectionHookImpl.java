package me.kermx.prismaUtils.integrations.protection.impl;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.kermx.prismaUtils.integrations.protection.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class TownyProtectionHookImpl implements IProtectionHook {

    private final Towny towny;

    public TownyProtectionHookImpl(Towny towny) {
        this.towny = towny;
    }

    public static TownyProtectionHookImpl createIfPresent(PluginManager pm) {
        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            return new TownyProtectionHookImpl((Towny) townyPlugin);
        }
        return null;
    }

    /**
     *
     * @param player
     * @param location
     * @return false if the player cannot build, true if they can
     */
    @Override
    public boolean canBuild(Player player, Location location) {
        return PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
    }
}
