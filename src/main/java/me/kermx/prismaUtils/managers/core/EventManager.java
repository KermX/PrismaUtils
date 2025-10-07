package me.kermx.prismaUtils.managers.core;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.event.Listener;

import java.util.*;

public class EventManager {
    private final PrismaUtils plugin;
    private final Map<FeatureToggleManager.Feature, List<Listener>> featureListeners = new HashMap<>();

    public EventManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void registerFeatureListeners(FeatureToggleManager.Feature feature, Listener... listeners) {
        if (plugin.getFeatureToggleManager().isEnabled(feature)) {
            registerListeners(listeners);
            featureListeners.computeIfAbsent(feature, k -> new ArrayList<>()).addAll(Arrays.asList(listeners));
        }
    }

}
