package me.kermx.prismaUtils.integrations.impl.protection;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.kermx.prismaUtils.integrations.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class TownyHookImpl implements IProtectionHook {

    private final Towny towny;

    public TownyHookImpl(Towny towny) {
        this.towny = towny;
    }

    public static TownyHookImpl createIfPresent(PluginManager pm) {
        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            return new TownyHookImpl((Towny) townyPlugin);
        }
        return null;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
    }
}
