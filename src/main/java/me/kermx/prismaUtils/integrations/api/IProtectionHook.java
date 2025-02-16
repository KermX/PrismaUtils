package me.kermx.prismaUtils.integrations.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IProtectionHook {
    boolean canBuild(Player player, Location location);
}
