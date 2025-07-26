package me.kermx.prismaUtils;

import me.kermx.prismaUtils.commands.admin.*;
import me.kermx.prismaUtils.commands.crafting.*;
import me.kermx.prismaUtils.commands.player.*;
import me.kermx.prismaUtils.commands.player.Tpask.*;
import me.kermx.prismaUtils.commands.player.homes.AdminHomesCommand;
import me.kermx.prismaUtils.commands.player.homes.DelHomeCommand;
import me.kermx.prismaUtils.commands.player.homes.HomesCommand;
import me.kermx.prismaUtils.commands.player.homes.SetHomeCommand;
import me.kermx.prismaUtils.commands.player.restore.*;
import me.kermx.prismaUtils.commands.player.teleport.*;
import me.kermx.prismaUtils.commands.player.warps.DelWarpCommand;
import me.kermx.prismaUtils.commands.player.warps.SetWarpCommand;
import me.kermx.prismaUtils.commands.player.warps.WarpCommand;
import me.kermx.prismaUtils.commands.utility.*;
import me.kermx.prismaUtils.handlers.block.ClimbableChainsHandler;
import me.kermx.prismaUtils.handlers.block.CopperOxidationHandler;
import me.kermx.prismaUtils.handlers.block.SeedAndShearBlocksHandler;
import me.kermx.prismaUtils.handlers.block.SilkSpawnerHandler;
import me.kermx.prismaUtils.handlers.mob.*;
import me.kermx.prismaUtils.handlers.player.*;
import me.kermx.prismaUtils.integrations.ProtectionService;
import me.kermx.prismaUtils.integrations.SitService;
import me.kermx.prismaUtils.integrations.TerritoryService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.managers.features.AfkManager;
import me.kermx.prismaUtils.managers.features.FlightManager;
import me.kermx.prismaUtils.managers.general.CommandManager;
import me.kermx.prismaUtils.managers.general.CooldownManager;
import me.kermx.prismaUtils.managers.general.EventManager;
import me.kermx.prismaUtils.managers.features.DisabledCraftingRecipesManager;
import me.kermx.prismaUtils.managers.features.SeenManager;
import me.kermx.prismaUtils.managers.general.configs.WarpsConfigManager;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.placeholders.AfkPlaceholderExpansion;
import me.kermx.prismaUtils.placeholders.MiniMessagePlaceholderExpansion;
import me.kermx.prismaUtils.placeholders.UnixLocalTimeExpansion;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {

    // TODO - much later: Major refactor of event handling!! Wooooooo
    // TODO: Fix the stupid patrol command. Always says there are no players
    // TODO: Add configuration options for AntiAutoFishingHandler

    private PlayerDataManager playerDataManager;
    private ProtectionService protectionService;
    private TerritoryService territoryService;
    private SitService sitService;
    private TeleportRequestManager teleportRequestManager;
    private SeedAndShearBlocksHandler seedAndShearBlocksHandler;
    private SeenManager seenManager;
    private GodCommand godCommand;
    private AfkManager afkManager;
    private FlightManager flightManager;

    @Override
    public void onEnable() {
        loadConfigurations();

        // Initialize services
        protectionService = new ProtectionService(getServer().getPluginManager());
        sitService = new SitService(getServer().getPluginManager(), getLogger());
        territoryService = new TerritoryService(getServer().getPluginManager(), getLogger());

        // Initialize specific managers / handlers
        teleportRequestManager = new TeleportRequestManager(this);
        seedAndShearBlocksHandler = new SeedAndShearBlocksHandler(protectionService);
        seenManager = new SeenManager();

        // Initialize CooldownManager singleton
        CooldownManager.getInstance();

        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);

        afkManager = new AfkManager(this);

        flightManager = new FlightManager(this, territoryService);

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

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllData();
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
        PatrolCommand patrolCommand = new PatrolCommand();
        commandManager.registerCommand("patrol", patrolCommand, patrolCommand);

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
        // Homes Commands
        HomesCommand homesCommand = new HomesCommand(this);
        commandManager.registerCommand("homes", homesCommand, homesCommand);
        commandManager.registerCommand("home", homesCommand, homesCommand);
        SetHomeCommand setHomeCommand = new SetHomeCommand(this, homesCommand);
        commandManager.registerCommand("sethome", setHomeCommand, setHomeCommand);
        DelHomeCommand delHomeCommand = new DelHomeCommand(this, homesCommand);
        commandManager.registerCommand("delhome", delHomeCommand, delHomeCommand);
        AdminHomesCommand adminHomesCommand = new AdminHomesCommand(this, homesCommand);
        commandManager.registerCommand("adminhome", adminHomesCommand, adminHomesCommand);
        // Fly Commands
        FlyCommand flyCommand = new FlyCommand(flightManager);
        commandManager.registerCommand("fly", flyCommand, flyCommand);
        FlyTimeCommand flyTimeCommand = new FlyTimeCommand(flightManager);
        commandManager.registerCommand("flytime", flyTimeCommand, flyTimeCommand);
        TempFlyCommand tempFlyCommand = new TempFlyCommand(flightManager);
        commandManager.registerCommand("tempfly", tempFlyCommand, tempFlyCommand);
        // Other Teleport Commands
        BackCommand backCommand = new BackCommand(playerDataManager);
        commandManager.registerCommand("back", backCommand, backCommand);
        SpawnCommand spawnCommand = new SpawnCommand(this);
        commandManager.registerCommand("spawn", spawnCommand, spawnCommand);
        TpCommand tpCommand = new TpCommand(this);
        commandManager.registerCommand("tp", tpCommand, tpCommand);
        TpHereCommand tpHereCommand = new TpHereCommand(this);
        commandManager.registerCommand("tphere", tpHereCommand, tpHereCommand);
        TpPosCommand tpPosCommand = new TpPosCommand(this);
        commandManager.registerCommand("tppos", tpPosCommand, tpPosCommand);
        // Warp Commands
        WarpsConfigManager warpsConfigManager = ConfigManager.getInstance().getWarpsConfig();
        WarpCommand warpCommand = new WarpCommand(warpsConfigManager, this);
        commandManager.registerCommand("warp", warpCommand, warpCommand);
        SetWarpCommand setWarpCommand = new SetWarpCommand(warpsConfigManager);
        commandManager.registerCommand("setwarp", setWarpCommand, setWarpCommand);
        DelWarpCommand delWarpCommand = new DelWarpCommand(warpsConfigManager);
        commandManager.registerCommand("delwarp", delWarpCommand, delWarpCommand);
        // Teleport request commands
        TpaCommand tpaCommand = new TpaCommand(teleportRequestManager);
        commandManager.registerCommand("tpa", tpaCommand, tpaCommand);
        TpaHereCommand tpaHereCommand = new TpaHereCommand(teleportRequestManager);
        commandManager.registerCommand("tpahere", tpaHereCommand, tpaHereCommand);
        TpAcceptCommand tpAcceptCommand = new TpAcceptCommand(teleportRequestManager, this);
        commandManager.registerCommand("tpaccept", tpAcceptCommand, tpAcceptCommand);
        TpDenyCommand tpDenyCommand = new TpDenyCommand(teleportRequestManager);
        commandManager.registerCommand("tpdeny", tpDenyCommand, tpDenyCommand);
        TpCancelCommand tpCancelCommand = new TpCancelCommand(teleportRequestManager);
        commandManager.registerCommand("tpcancel", tpCancelCommand, tpCancelCommand);
        // Afk Commands
        AfkCommand afkCommand = new AfkCommand(afkManager);
        commandManager.registerCommand("afk", afkCommand, afkCommand);

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
        MailCommand mailCommand = new MailCommand(this);
        commandManager.registerCommand("mail", mailCommand, mailCommand);
        PlayTimeCommand playTimeCommand = new PlayTimeCommand();
        commandManager.registerCommand("playtime", playTimeCommand, playTimeCommand);
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
                new SafeSpawnEggHandler(),
                seedAndShearBlocksHandler,
                new CopperOxidationHandler(protectionService),
                new CuffCommand(),
                new HorseZombificationHandler(),
                new PermissionKeepInvHandler(),
                new RespawnMessageHandler(),
                new LastLocationHandler(playerDataManager),
                new AntiAutoFishingHandler(this)
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
            eventManager.registerListeners(new EntityLightningImmunityHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().chainsAreClimbable) {
            eventManager.registerListeners(new ClimbableChainsHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().enableNonLevelBasedEnchanting) {
            eventManager.registerListeners(new AlternativeEnchantingCostHandler());
        }
        if (ConfigManager.getInstance().getMainConfig().disableNetherMobZombification) {
            eventManager.registerListeners(new NetherMobZombificationHandler());
        }
        if (ConfigManager.getInstance().getAfkConfig().afkEnabled) {
            eventManager.registerListeners(new AfkProtectionListener(afkManager, ConfigManager.getInstance().getAfkConfig()), afkManager);
        }
    }

    private void doStartupOperations() {
        new DisabledCraftingRecipesManager().removeConfiguredRecipes();
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MiniMessagePlaceholderExpansion().register();
            new UnixLocalTimeExpansion().register();
            new AfkPlaceholderExpansion(afkManager).register();
        } else {
            getLogger().warning("Placeholder API doesn't exist! HELP!!!");
        }
    }

    private void startTasks() {
        new AfkTitlesHandler().runTaskTimer(this, 0, 80);
    }

    public SitService getSitService() {
        return sitService;
    }
}
