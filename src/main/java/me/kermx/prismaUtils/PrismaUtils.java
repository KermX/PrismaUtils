package me.kermx.prismaUtils;

import me.kermx.prismaUtils.commands.admin.*;
import me.kermx.prismaUtils.commands.crafting.*;
import me.kermx.prismaUtils.commands.player.*;
import me.kermx.prismaUtils.commands.player.restore.*;
import me.kermx.prismaUtils.commands.utility.*;
import me.kermx.prismaUtils.handlers.block.ClimbableChainsHandler;
import me.kermx.prismaUtils.handlers.block.CopperOxidationHandler;
import me.kermx.prismaUtils.handlers.block.SeedAndShearBlocksHandler;
import me.kermx.prismaUtils.handlers.block.SilkSpawnerHandler;
import me.kermx.prismaUtils.handlers.mob.*;
import me.kermx.prismaUtils.handlers.player.*;
import me.kermx.prismaUtils.integrations.flight.FlightHandler;
import me.kermx.prismaUtils.integrations.protection.ProtectionHandler;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.managers.general.CommandManager;
import me.kermx.prismaUtils.managers.general.EventManager;
import me.kermx.prismaUtils.managers.features.DisabledCraftingRecipesManager;
import me.kermx.prismaUtils.managers.features.SeenManager;
import me.kermx.prismaUtils.placeholders.MiniMessagePlaceholderExpansion;
import me.kermx.prismaUtils.placeholders.UnixLocalTimeExpansion;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private ProtectionHandler protectionHandler;
    private FlightHandler flightHandler;
    private SeedAndShearBlocksHandler seedAndShearBlocksHandler;
    private SeenManager seenManager;
    private GodCommand godCommand;

    @Override
    public void onEnable() {
        loadConfigurations();

        // Initialize ProtectionHandler
        protectionHandler = new ProtectionHandler(getServer().getPluginManager(), getLogger());

        // Initialize FlightHandler (if needed)
        flightHandler = new FlightHandler(getServer().getPluginManager(), getLogger());


        // Initialize specific managers / handlers
        seedAndShearBlocksHandler = new SeedAndShearBlocksHandler(protectionHandler);
        seenManager = new SeenManager();

        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);


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
        for (Player player : getServer().getOnlinePlayers()) {
            playerDataManager.savePlayerData(player.getUniqueId());
        }
    }

    private void loadConfigurations() {
        ConfigManager.initialize(this);
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
        PlayerHeadCommand playerHeadCommand = new PlayerHeadCommand();
        commandManager.registerCommand("playerhead", playerHeadCommand, playerHeadCommand);
        SpawnMobCommand spawnMobCommand = new SpawnMobCommand();
        commandManager.registerCommand("spawnmob", spawnMobCommand, spawnMobCommand);
        CuffCommand cuffCommand = new CuffCommand();
        commandManager.registerCommand("cuff", cuffCommand, cuffCommand);
        SmiteCommand smiteCommand = new SmiteCommand();
        commandManager.registerCommand("smite", smiteCommand, smiteCommand);
        ClearMobsCommand clearMobsCommand = new ClearMobsCommand();
        commandManager.registerCommand("clearmobs", clearMobsCommand, clearMobsCommand);
        UptimeCommand uptimeCommand = new UptimeCommand();
        commandManager.registerCommand("uptime", uptimeCommand, uptimeCommand);
        RoundRotationCommand roundRotationCommand = new RoundRotationCommand();
        commandManager.registerCommand("roundrotation", roundRotationCommand, roundRotationCommand);

        // Player Commands
        BottomCommand bottomCommand = new BottomCommand();
        commandManager.registerCommand("bottom", bottomCommand, bottomCommand);
        RestoreHungerCommand restoreHungerCommand = new RestoreHungerCommand();
        commandManager.registerCommand("feed", restoreHungerCommand, restoreHungerCommand);
        FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
        commandManager.registerCommand("flyspeed", flySpeedCommand, flySpeedCommand);
        // special case for god command, includes event listener
        commandManager.registerCommand("god", godCommand, godCommand);
        RestoreHealthCommand restoreHealthCommand = new RestoreHealthCommand();
        commandManager.registerCommand("heal", restoreHealthCommand, restoreHealthCommand);
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
        CondenseCommand condenseCommand = new CondenseCommand();
        commandManager.registerCommand("condense", condenseCommand, condenseCommand);
        UncondenseCommand uncondenseCommand = new UncondenseCommand();
        commandManager.registerCommand("uncondense", uncondenseCommand, uncondenseCommand);
        TrashCommand trashCommand = new TrashCommand();
        commandManager.registerCommand("trash", trashCommand, trashCommand);
        MeasureDistanceCommand measureDistanceCommand = new MeasureDistanceCommand();
        commandManager.registerCommand("measure", measureDistanceCommand, measureDistanceCommand);
        MassDisenchantCommand massDisenchantCommand = new MassDisenchantCommand();
        commandManager.registerCommand("disenchant", massDisenchantCommand, massDisenchantCommand);
        LightLevelCommand lightLevelCommand = new LightLevelCommand();
        commandManager.registerCommand("lightlevel", lightLevelCommand, lightLevelCommand);
        ExtinguishCommand extinguishCommand = new ExtinguishCommand();
        commandManager.registerCommand("extinguish", extinguishCommand, extinguishCommand);
        LimitsCommand limitsCommand = new LimitsCommand(this);
        commandManager.registerCommand("limits", limitsCommand, limitsCommand);
    }

    private void registerEvents(EventManager eventManager) {
        eventManager.registerListeners(
                new PlayerDataListener(playerDataManager),
                new RemoveDropsHandler(),
                new SlimeSplitHandler(),
                new CustomDeathMessageHandler(),
                new HealthScaleHandler(),
                new FirstJoinCommandsHandler(this),
                new FirstJoinSpawnHandler(),
                new SilkSpawnerHandler(),
                seedAndShearBlocksHandler,
                new CopperOxidationHandler(protectionHandler),
                new CuffCommand(),
                new HorseZombificationHandler(),
                new PermissionKeepInvHandler(),
                new EnhancedTownyFlightHandler(flightHandler,playerDataManager)
        );

        // Register config conditional events
        registerConfigConditionalEvents(eventManager);

        // Register seen event
        eventManager.registerListeners(new SeenEventsHandler(seenManager));

        // Register the GodCommand as an event listener using the same instance
        eventManager.registerListeners(godCommand);
    }

    private void registerConfigConditionalEvents(EventManager eventManager) {
        if (ConfigManager.getInstance().getMainConfig().disableSpawnerMobItemDrops) {
            eventManager.registerListeners(new SpawnerMobItemDropsHandler(this));
        }
        if (ConfigManager.getInstance().getMainConfig().endermitesImmuneToLightning) {
            eventManager.registerListeners(new EndermiteImmunityHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().chainsAreClimbable) {
            eventManager.registerListeners(new ClimbableChainsHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().enableNonLevelBasedEnchanting) {
            eventManager.registerListeners(new NonLevelBasedEnchantingHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().disableNetherMobZombification) {
            eventManager.registerListeners(new NetherMobZombificationHandler());
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
        new AfkTitlesHandler().runTaskTimer(this, 0, 80);
    }
}
