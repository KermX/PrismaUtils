package me.kermx.prismaUtils;

import me.kermx.prismaUtils.Commands.CraftingStationCommands.*;
import me.kermx.prismaUtils.Commands.OtherCommands.*;
import me.kermx.prismaUtils.Handlers.CustomDeathMessageHandler;
import me.kermx.prismaUtils.Handlers.NetherMobZombificationHandler;
import me.kermx.prismaUtils.Handlers.RemoveDropsHandler;
import me.kermx.prismaUtils.Handlers.SlimeSplitHandler;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    private ConfigUtils configUtils;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigurations();

        registerCommands();
        registerTabCompletions();
        registerEvents();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfigurations(){
        configUtils = new ConfigUtils(this);
        configUtils.loadConfig();
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
        getCommand("itemname").setExecutor(new ItemNameCommand());
        getCommand("near").setExecutor(new NearCommand());
        getCommand("repair").setExecutor(new RepairCommand());
        getCommand("flyspeed").setExecutor(new FlySpeedCommand());

        getCommand("prismautilsreload").setExecutor(new ReloadConfigCommand(this));
        getCommand("ping").setExecutor(new PingCommand());
    }

    public void registerTabCompletions(){
        getCommand("feed").setTabCompleter(new FeedCommand());
        getCommand("heal").setTabCompleter(new HealCommand());
        getCommand("repair").setTabCompleter(new RepairCommand());
        getCommand("flyspeed").setTabCompleter(new FlySpeedCommand());
    }

    public void registerEvents(){
        getServer().getPluginManager().registerEvents(new RemoveDropsHandler(), this);
        getServer().getPluginManager().registerEvents(new NetherMobZombificationHandler(), this);
        getServer().getPluginManager().registerEvents(new SlimeSplitHandler(), this);
        getServer().getPluginManager().registerEvents(new CustomDeathMessageHandler(configUtils), this);
    }
}
