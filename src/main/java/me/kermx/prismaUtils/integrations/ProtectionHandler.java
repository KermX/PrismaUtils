package me.kermx.prismaUtils.integrations;

import me.kermx.prismaUtils.integrations.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtectionHandler {

    private final List<IProtectionHook> hooks = new ArrayList<>();
    private final Logger logger;

    public ProtectionHandler(PluginManager pm, Logger logger) {
        this.logger = logger;
        loadIntegration(pm, "WorldGuard", "me.kermx.prismaUtils.integrations.impl.protection.WorldGuardHookImpl");
        loadIntegration(pm, "Towny", "me.kermx.prismaUtils.integrations.impl.protection.TownyHookImpl");
    }

    /**
     * Attempts to load an integration using reflection.
     *
     * @param pm         The PluginManager instance.
     * @param pluginName The name of the plugin (e.g., "WorldGuard").
     * @param className  The fully qualified class name of the integration.
     */
    private void loadIntegration(PluginManager pm, String pluginName, String className) {
        Plugin plugin = pm.getPlugin(pluginName);
        if (plugin != null && plugin.isEnabled()) {
            try {
                Class<?> clazz = Class.forName(className);
                Method createMethod = clazz.getMethod("createIfPresent", PluginManager.class);
                IProtectionHook hook = (IProtectionHook) createMethod.invoke(null, pm);
                if (hook != null) {
                    hooks.add(hook);
                    logger.log(Level.INFO, "Loaded protection integration for " + pluginName);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load integration for " + pluginName, e);
            }
        } else {
            logger.log(Level.INFO, pluginName + " not found or disabled, skipping integration.");
        }
    }

    /**
     * Checks if the block at the specified location is protected by any loaded protection plugin.
     *
     * @param player   The player trying to build.
     * @param location The location to check.
     * @return true if any integration denies the build, false otherwise.
     */
    public boolean blockIsProtectedByPlugin(Player player, Location location) {
        for (IProtectionHook hook : hooks) {
            if (!hook.canBuild(player, location)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Allows manual registration of additional protection hooks.
     * Not necessary for most implementations.
     *
     * @param hook The protection hook to register.
     */
    public void registerHook(IProtectionHook hook) {
        if (hook != null) {
            hooks.add(hook);
        }
    }
}