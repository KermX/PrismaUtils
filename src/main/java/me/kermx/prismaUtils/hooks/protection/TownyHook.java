package me.kermx.prismaUtils.hooks.protection;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class TownyHook {

    private final Towny towny;

    public TownyHook(Towny towny) {
        this.towny = towny;
    }

    public static TownyHook createIfPresent(PluginManager pm) {
        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            return new TownyHook((Towny) townyPlugin);
        } else {
            return null;
        }
    }

    public boolean canBuildTowny(Player player, Location location) {
        return PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
    }


}
