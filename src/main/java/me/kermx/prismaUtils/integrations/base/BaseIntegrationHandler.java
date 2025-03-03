package me.kermx.prismaUtils.integrations.base;

import me.kermx.prismaUtils.integrations.base.api.IIntegrationHook;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseIntegrationHandler<T extends IIntegrationHook> {

    protected final List<T> hooks = new ArrayList<>();
    private final Logger logger;

    public BaseIntegrationHandler(Logger logger) {
        this.logger = logger;
    }

    /**
     * Loads an integration hook dynamically using reflection.
     *
     * @param pm         The PluginManager instance.
     * @param pluginName The name of the plugin.
     * @param className  The fully qualified class name of the hook implementation.
     */
    protected void loadIntegration(PluginManager pm, String pluginName, String className) {
        Plugin plugin = pm.getPlugin(pluginName);
        if (plugin != null && plugin.isEnabled()) {
            try {
                Class<?> clazz = Class.forName(className);
                Method createMethod = clazz.getMethod("createIfPresent", PluginManager.class);
                T hook = (T) createMethod.invoke(null, pm);
                if (hook != null) {
                    hooks.add(hook);
                    logger.log(Level.INFO, "Loaded integration for " + pluginName);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load integration for " + pluginName, e);
            }
        } else {
            logger.log(Level.INFO, pluginName + " not found or disabled, skipping integration.");
        }
    }
    /**
     * Manually registers a hook (useful for testing or non-standard integrations).
     *
     * @param hook Integration hook.
     */
    public void registerHook(T hook) {
        if (hook != null) {
            hooks.add(hook);
        }
    }

    /**
     * Gets the list of loaded hooks.
     *
     * @return The list of hooks.
     */
    public List<T> getHooks() {
        return hooks;
    }
}

