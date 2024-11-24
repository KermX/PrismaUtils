package me.kermx.prismaUtils;

import me.kermx.prismaUtils.Commands.CraftingStationCommands.*;
import me.kermx.prismaUtils.Commands.OtherCommands.FeedCommand;
import me.kermx.prismaUtils.Commands.OtherCommands.HealCommand;
import me.kermx.prismaUtils.Commands.OtherCommands.NearCommand;
import me.kermx.prismaUtils.Handlers.NetherMobZombificationHandler;
import me.kermx.prismaUtils.Handlers.RemoveDropsHandler;
import me.kermx.prismaUtils.Handlers.SlimeSplitHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerCommands(){
        getCommand("craftingtable").setExecutor(new CraftingTableCommand());
        getCommand("anvil").setExecutor(new AnvilCommand());
        getCommand("enchantingtable").setExecutor(new EnchantingTableCommand());
        getCommand("stonecutter").setExecutor(new StonecutterCommand());
        getCommand("smithingtable").setExecutor(new SmithingTableCommand());
        getCommand("loom").setExecutor(new LoomCommand());
        getCommand("grindstone").setExecutor(new GrindstoneCommand());
        getCommand("cartographytable").setExecutor(new CartographyTableCommand());

        getCommand("feed").setExecutor(new FeedCommand());
        getCommand("heal").setExecutor(new HealCommand());
        getCommand("near").setExecutor(new NearCommand());
    }

    public void registerEvents(){
        getServer().getPluginManager().registerEvents(new RemoveDropsHandler(), this);
        getServer().getPluginManager().registerEvents(new NetherMobZombificationHandler(), this);
        getServer().getPluginManager().registerEvents(new SlimeSplitHandler(), this);
    }
}
