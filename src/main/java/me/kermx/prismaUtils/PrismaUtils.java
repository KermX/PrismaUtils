package me.kermx.prismaUtils;

import me.kermx.prismaUtils.Commands.AdminCommands.*;
import me.kermx.prismaUtils.Commands.CraftingStationCommands.*;
import me.kermx.prismaUtils.Commands.OtherCommands.*;
import me.kermx.prismaUtils.Handlers.*;
import me.kermx.prismaUtils.Managers.DisabledCraftingRecipesManager;
import me.kermx.prismaUtils.Managers.SeenManager;
import me.kermx.prismaUtils.Placeholders.MiniMessagePlaceholderExpansion;
import me.kermx.prismaUtils.Placeholders.UnixLocalTimeExpansion;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    private SeedAndShearBlocksHandler seedAndShearBlocksHandler;
    private SeenManager seenManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfigurations();

        seedAndShearBlocksHandler = new SeedAndShearBlocksHandler();
        seedAndShearBlocksHandler.registerTransformations();
        seenManager = new SeenManager();

        doStartupOperations();
        registerPlaceholders();
        registerCommands();
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
        // Crafting Station Commands
        AnvilCommand anvilCommand = new AnvilCommand();
        getCommand("anvil").setExecutor(anvilCommand);
        getCommand("anvil").setTabCompleter(anvilCommand);

        CartographyTableCommand cartographyTableCommand = new CartographyTableCommand();
        getCommand("cartographytable").setExecutor(cartographyTableCommand);
        getCommand("cartographytable").setTabCompleter(cartographyTableCommand);

        CraftingTableCommand craftingTableCommand = new CraftingTableCommand();
        getCommand("craftingtable").setExecutor(craftingTableCommand);
        getCommand("craftingtable").setTabCompleter(craftingTableCommand);

        EnchantingTableCommand enchantingTableCommand = new EnchantingTableCommand();
        getCommand("enchantingtable").setExecutor(enchantingTableCommand);
        getCommand("enchantingtable").setTabCompleter(enchantingTableCommand);

        EnderChestCommand enderChestCommand = new EnderChestCommand();
        getCommand("enderchest").setExecutor(enderChestCommand);
        getCommand("enderchest").setTabCompleter(enderChestCommand);

        GrindstoneCommand grindstoneCommand = new GrindstoneCommand();
        getCommand("grindstone").setExecutor(grindstoneCommand);
        getCommand("grindstone").setTabCompleter(grindstoneCommand);

        LoomCommand loomCommand = new LoomCommand();
        getCommand("loom").setExecutor(loomCommand);
        getCommand("loom").setTabCompleter(loomCommand);

        SmithingTableCommand smithingTableCommand = new SmithingTableCommand();
        getCommand("smithingtable").setExecutor(smithingTableCommand);
        getCommand("smithingtable").setTabCompleter(smithingTableCommand);

        StonecutterCommand stonecutterCommand = new StonecutterCommand();
        getCommand("stonecutter").setExecutor(stonecutterCommand);
        getCommand("stonecutter").setTabCompleter(stonecutterCommand);

        // Admin Commands
        BlockInfoCommand blockInfoCommand = new BlockInfoCommand();
        getCommand("blockinfo").setExecutor(blockInfoCommand);
        getCommand("blockinfo").setTabCompleter(blockInfoCommand);

        EntityInfoCommand entityInfoCommand = new EntityInfoCommand();
        getCommand("entityinfo").setExecutor(entityInfoCommand);
        getCommand("entityinfo").setTabCompleter(entityInfoCommand);

        ItemInfoCommand itemInfoCommand = new ItemInfoCommand();
        getCommand("iteminfo").setExecutor(itemInfoCommand);
        getCommand("iteminfo").setTabCompleter(itemInfoCommand);

        ReloadConfigCommand reloadConfigCommand = new ReloadConfigCommand(this);
        getCommand("prismautilsreload").setExecutor(reloadConfigCommand);
        getCommand("prismautilsreload").setTabCompleter(reloadConfigCommand);

        SetModelDataCommand setModelDataCommand = new SetModelDataCommand();
        getCommand("setmodeldata").setExecutor(setModelDataCommand);
        getCommand("setmodeldata").setTabCompleter(setModelDataCommand);

        // Other Commands
        BottomCommand bottomCommand = new BottomCommand();
        getCommand("bottom").setExecutor(bottomCommand);
        getCommand("bottom").setTabCompleter(bottomCommand);

        FeedCommand feedCommand = new FeedCommand();
        getCommand("feed").setExecutor(feedCommand);
        getCommand("feed").setTabCompleter(feedCommand);

        FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
        getCommand("flyspeed").setExecutor(flySpeedCommand);
        getCommand("flyspeed").setTabCompleter(flySpeedCommand);

        GodCommand godCommand = new GodCommand();
        getCommand("god").setExecutor(godCommand);
        getCommand("god").setTabCompleter(godCommand);
        getServer().getPluginManager().registerEvents(godCommand, this);

        HealCommand healCommand = new HealCommand();
        getCommand("heal").setExecutor(healCommand);
        getCommand("heal").setTabCompleter(healCommand);

        ItemNameCommand itemNameCommand = new ItemNameCommand();
        getCommand("itemname").setExecutor(itemNameCommand);
        getCommand("itemname").setTabCompleter(itemNameCommand);

        NearCommand nearCommand = new NearCommand();
        getCommand("near").setExecutor(nearCommand);
        getCommand("near").setTabCompleter(nearCommand);

        pTimeCommand pTimeCommand = new pTimeCommand();
        getCommand("ptime").setExecutor(pTimeCommand);
        getCommand("ptime").setTabCompleter(pTimeCommand);

        pWeatherCommand pWeatherCommand = new pWeatherCommand();
        getCommand("pweather").setExecutor(pWeatherCommand);
        getCommand("pweather").setTabCompleter(pWeatherCommand);

        RepairCommand repairCommand = new RepairCommand();
        getCommand("repair").setExecutor(repairCommand);
        getCommand("repair").setTabCompleter(repairCommand);

        TopCommand topCommand = new TopCommand();
        getCommand("top").setExecutor(topCommand);
        getCommand("top").setTabCompleter(topCommand);

        SeenManager seenManager = new SeenManager();
        SeenCommand seenCommand = new SeenCommand(seenManager);
        getCommand("seen").setExecutor(seenCommand);
        getCommand("seen").setTabCompleter(seenCommand);
    }

    public void registerEvents(){
        getServer().getPluginManager().registerEvents(new RemoveDropsHandler(), this);
        getServer().getPluginManager().registerEvents(new NetherMobZombificationHandler(), this);
        getServer().getPluginManager().registerEvents(new SlimeSplitHandler(), this);
        getServer().getPluginManager().registerEvents(new CustomDeathMessageHandler(), this);
//        getServer().getPluginManager().registerEvents(godCommand, this);
        getServer().getPluginManager().registerEvents(new HealthScaleHandler(), this);
        getServer().getPluginManager().registerEvents(new FirstJoinCommandsHandler(this), this);
        getServer().getPluginManager().registerEvents(new FirstJoinSpawnHandler(), this);
        getServer().getPluginManager().registerEvents(new SilkSpawnerHandler(), this);
        getServer().getPluginManager().registerEvents(seedAndShearBlocksHandler, this);
        getServer().getPluginManager().registerEvents(new CopperOxidationHandler(), this);
        getServer().getPluginManager().registerEvents(new SeenEventsHandler(seenManager), this);
        registerConfigConditionalEvents();
    }

    public void registerConfigConditionalEvents(){
        if (ConfigUtils.getInstance().disableSpawnerMobItemDrops){
            getServer().getPluginManager().registerEvents(new SpawnerMobItemDropsHandler(this), this);
        }
        if (ConfigUtils.getInstance().endermitesImmuneToLightning){
            getServer().getPluginManager().registerEvents(new EndermiteImmunityHandler(), this);
        }
    }

    public void doStartupOperations(){
        new DisabledCraftingRecipesManager().removeConfiguredRecipes();
        new SeedAndShearBlocksHandler().registerTransformations();
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
