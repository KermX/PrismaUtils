package me.kermx.prismaUtils;

import me.kermx.prismaUtils.Commands.admin.*;
import me.kermx.prismaUtils.Commands.crafting.*;
import me.kermx.prismaUtils.Commands.player.*;
import me.kermx.prismaUtils.Commands.utility.*;
import me.kermx.prismaUtils.handlers.block.CopperOxidationHandler;
import me.kermx.prismaUtils.handlers.block.SeedAndShearBlocksHandler;
import me.kermx.prismaUtils.handlers.block.SilkSpawnerHandler;
import me.kermx.prismaUtils.handlers.mob.*;
import me.kermx.prismaUtils.handlers.player.*;
import me.kermx.prismaUtils.managers.general.CommandManager;
import me.kermx.prismaUtils.managers.general.EventManager;
import me.kermx.prismaUtils.managers.features.DisabledCraftingRecipesManager;
import me.kermx.prismaUtils.managers.features.SeenManager;
import me.kermx.prismaUtils.placeholders.MiniMessagePlaceholderExpansion;
import me.kermx.prismaUtils.placeholders.UnixLocalTimeExpansion;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    private SeedAndShearBlocksHandler seedAndShearBlocksHandler;
    private SeenManager seenManager;
    private GodCommand godCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfigurations();

        // Initialize specific managers / handlers
        seedAndShearBlocksHandler = new SeedAndShearBlocksHandler();
        seenManager = new SeenManager();

        doStartupOperations();
        registerPlaceholders();

        godCommand = new GodCommand();

        // Use CommandManager and EventManager to register commands and events.
        CommandManager commandManager = new CommandManager(this);
        registerCommands(commandManager);

        EventManager eventManager = new EventManager(this);
        registerEvents(eventManager);

        startTasks();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfigurations() {
        ConfigManager.initialize(this);
        ConfigManager.getInstance().loadConfig();
    }

    private void registerCommands(CommandManager commandManager) {
        // Crafting Station Commands
        AnvilCommand anvilCommand = new AnvilCommand();
        commandManager.registerCommand("anvil", anvilCommand, anvilCommand);
        CartographyTableCommand cartographyTableCommand = new CartographyTableCommand();
        commandManager.registerCommand("cartographytable", cartographyTableCommand, cartographyTableCommand);
        CraftingTableCommand craftingTableCommand = new CraftingTableCommand();
        commandManager.registerCommand("craftingtable", craftingTableCommand, craftingTableCommand);
        EnchantingTableCommand enchantingTableCommand = new EnchantingTableCommand();
        commandManager.registerCommand("enchantingtable", enchantingTableCommand, enchantingTableCommand);
        EnderChestCommand enderChestCommand = new EnderChestCommand();
        commandManager.registerCommand("enderchest", enderChestCommand, enderChestCommand);
        GrindstoneCommand grindstoneCommand = new GrindstoneCommand();
        commandManager.registerCommand("grindstone", grindstoneCommand, grindstoneCommand);
        LoomCommand loomCommand = new LoomCommand();
        commandManager.registerCommand("loom", loomCommand, loomCommand);
        SmithingTableCommand smithingTableCommand = new SmithingTableCommand();
        commandManager.registerCommand("smithingtable", smithingTableCommand, smithingTableCommand);
        StonecutterCommand stonecutterCommand = new StonecutterCommand();
        commandManager.registerCommand("stonecutter", stonecutterCommand, stonecutterCommand);

        // Admin Commands
        BlockInfoCommand blockInfoCommand = new BlockInfoCommand();
        commandManager.registerCommand("blockinfo", blockInfoCommand, blockInfoCommand);
        EntityInfoCommand entityInfoCommand = new EntityInfoCommand();
        commandManager.registerCommand("entityinfo", entityInfoCommand, entityInfoCommand);
        ItemInfoCommand itemInfoCommand = new ItemInfoCommand();
        commandManager.registerCommand("iteminfo", itemInfoCommand, itemInfoCommand);
        ReloadConfigCommand reloadConfigCommand = new ReloadConfigCommand(this);
        commandManager.registerCommand("prismautilsreload", reloadConfigCommand, reloadConfigCommand);
        SetModelDataCommand setModelDataCommand = new SetModelDataCommand();
        commandManager.registerCommand("setmodeldata", setModelDataCommand, setModelDataCommand);

        // Player Commands
        BottomCommand bottomCommand = new BottomCommand();
        commandManager.registerCommand("bottom", bottomCommand, bottomCommand);
        FeedCommand feedCommand = new FeedCommand();
        commandManager.registerCommand("feed", feedCommand, feedCommand);
        FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
        commandManager.registerCommand("flyspeed", flySpeedCommand, flySpeedCommand);
        // special case for god command, includes event listener
        commandManager.registerCommand("god", godCommand, godCommand);
        HealCommand healCommand = new HealCommand();
        commandManager.registerCommand("heal", healCommand, healCommand);
        pTimeCommand pTimeCommand = new pTimeCommand();
        commandManager.registerCommand("ptime", pTimeCommand, pTimeCommand);
        pWeatherCommand pWeatherCommand = new pWeatherCommand();
        commandManager.registerCommand("pweather", pWeatherCommand, pWeatherCommand);
        TopCommand topCommand = new TopCommand();
        commandManager.registerCommand("top", topCommand, topCommand);

        // Utility Commands
        ItemNameCommand itemNameCommand = new ItemNameCommand();
        commandManager.registerCommand("itemname", itemNameCommand, itemNameCommand);
        NearCommand nearCommand = new NearCommand();
        commandManager.registerCommand("near", nearCommand, nearCommand);
        PingCommand pingCommand = new PingCommand();
        commandManager.registerCommand("ping", pingCommand, pingCommand);
        RepairCommand repairCommand = new RepairCommand();
        commandManager.registerCommand("repair", repairCommand, repairCommand);
        SeenCommand seenCommand = new SeenCommand(seenManager);
        commandManager.registerCommand("seen", seenCommand, seenCommand);
    }

    private void registerEvents(EventManager eventManager) {
        eventManager.registerListeners(
                new RemoveDropsHandler(),
                new NetherMobZombificationHandler(),
                new SlimeSplitHandler(),
                new CustomDeathMessageHandler(),
                new HealthScaleHandler(),
                new FirstJoinCommandsHandler(this),
                new FirstJoinSpawnHandler(),
                new SilkSpawnerHandler(),
                seedAndShearBlocksHandler,
                new CopperOxidationHandler()
        );

        // Register config conditional events
        registerConfigConditionalEvents(eventManager);

        // Register seen event
        eventManager.registerListeners(new SeenEventsHandler(seenManager));

        // Register the GodCommand as an event listener using the same instance
        eventManager.registerListeners(godCommand);
    }

    private void registerConfigConditionalEvents(EventManager eventManager) {
        if (ConfigManager.getInstance().disableSpawnerMobItemDrops) {
            eventManager.registerListeners(new SpawnerMobItemDropsHandler(this));
        }
        if (ConfigManager.getInstance().endermitesImmuneToLightning) {
            eventManager.registerListeners(new EndermiteImmunityHandler());
        }
    }

    private void doStartupOperations() {
        new DisabledCraftingRecipesManager().removeConfiguredRecipes();
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MiniMessagePlaceholderExpansion().register();
            new UnixLocalTimeExpansion().register();
        } else {
            getLogger().warning("Placeholder API doesn't exist! HELP!!!");
        }
    }

    private void startTasks() {
        new AfkTitlesHandler().runTaskTimer(this, 0, 40);
    }
}
