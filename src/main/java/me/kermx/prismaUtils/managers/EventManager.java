package me.kermx.prismaUtils.managers;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.event.Listener;

public class EventManager {
    private final PrismaUtils plugin;

    public EventManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

}
