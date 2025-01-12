package me.kermx.prismaUtils;

import me.kermx.prismaUtils.Commands.AdminCommands.*;
import me.kermx.prismaUtils.Commands.CraftingStationCommands.*;
import me.kermx.prismaUtils.Commands.OtherCommands.*;
import me.kermx.prismaUtils.Handlers.*;
import me.kermx.prismaUtils.Placeholders.MiniMessagePlaceholderExpansion;
import me.kermx.prismaUtils.Placeholders.UnixLocalTimeExpansion;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    private ConfigUtils configUtils;

    GodCommand godCommand = new GodCommand();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigurations();

        registerPlaceholders();
        registerCommands();
        registerTabCompletions();
        registerEvents();
        startTasks();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfigurations(){
        ConfigUtils.initialize(this);
        ConfigUtils.getInstance().loadConfig();
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
        getCommand("ptime").setExecutor(new pTimeCommand());
        getCommand("pweather").setExecutor(new pWeatherCommand());
        getCommand("repair").setExecutor(new RepairCommand());
        getCommand("flyspeed").setExecutor(new FlySpeedCommand());
        getCommand("top").setExecutor(new TopCommand());
        getCommand("bottom").setExecutor(new BottomCommand());
        getCommand("god").setExecutor(godCommand);

        getCommand("prismautilsreload").setExecutor(new ReloadConfigCommand(this));
        getCommand("setmodeldata").setExecutor(new SetModelDataCommand());
        getCommand("blockinfo").setExecutor(new BlockInfoCommand());
        getCommand("entityinfo").setExecutor( new EntityInfoCommand());
        getCommand("iteminfo").setExecutor(new ItemInfoCommand());
        getCommand("ping").setExecutor(new PingCommand());
    }

    public void registerTabCompletions(){
        getCommand("feed").setTabCompleter(new FeedCommand());
        getCommand("heal").setTabCompleter(new HealCommand());
        getCommand("repair").setTabCompleter(new RepairCommand());
        getCommand("flyspeed").setTabCompleter(new FlySpeedCommand());
        getCommand("ptime").setTabCompleter(new pTimeCommand());
        getCommand("pweather").setTabCompleter(new pWeatherCommand());
    }

    public void registerEvents(){
        getServer().getPluginManager().registerEvents(new RemoveDropsHandler(), this);
        getServer().getPluginManager().registerEvents(new SpawnerMobItemDropsHandler(this), this);
        getServer().getPluginManager().registerEvents(new NetherMobZombificationHandler(), this);
        getServer().getPluginManager().registerEvents(new SlimeSplitHandler(), this);
        getServer().getPluginManager().registerEvents(new CustomDeathMessageHandler(), this);
        getServer().getPluginManager().registerEvents(godCommand, this);
        getServer().getPluginManager().registerEvents(new HealthScaleHandler(), this);
        getServer().getPluginManager().registerEvents(new FirstJoinCommandsHandler(this), this);
        getServer().getPluginManager().registerEvents(new FirstJoinSpawnHandler(), this);
    }

    public void registerPlaceholders(){
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MiniMessagePlaceholderExpansion().register();
            new UnixLocalTimeExpansion().register();
        } else {
            getLogger().warning("Placeholder API doesn't exist! HELP!!!");
        }
    }

    public void startTasks(){
        new AfkTitlesHandler().runTaskTimer(this, 0, 40);
    }
}
