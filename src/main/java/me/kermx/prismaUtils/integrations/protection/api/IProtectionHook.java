package me.kermx.prismaUtils.integrations.protection.api;

import me.kermx.prismaUtils.integrations.base.api.IIntegrationHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IProtectionHook extends IIntegrationHook {
    boolean canBuild(Player player, Location location);
}

