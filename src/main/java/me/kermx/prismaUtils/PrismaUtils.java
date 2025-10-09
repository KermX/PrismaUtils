package me.kermx.prismaUtils;

import me.kermx.prismaUtils.commands.admin.*;
import me.kermx.prismaUtils.commands.crafting.*;
import me.kermx.prismaUtils.commands.player.*;
import me.kermx.prismaUtils.commands.player.tpask.*;
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
import me.kermx.prismaUtils.services.ProtectionService;
import me.kermx.prismaUtils.services.SitService;
import me.kermx.prismaUtils.services.TerritoryService;
import me.kermx.prismaUtils.managers.core.*;
import me.kermx.prismaUtils.managers.playerdata.PlayerDataManager;
import me.kermx.prismaUtils.managers.feature.AfkManager;
import me.kermx.prismaUtils.managers.feature.FlightManager;
import me.kermx.prismaUtils.managers.feature.DisabledCraftingRecipesManager;
import me.kermx.prismaUtils.managers.feature.SeenManager;
import me.kermx.prismaUtils.managers.config.WarpsConfigManager;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.placeholders.*;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrismaUtils extends JavaPlugin {


    private PlayerDataManager playerDataManager;
    private FeatureToggleManager featureToggleManager;
    private ProtectionService protectionService;
    private TerritoryService territoryService;
    private SitService sitService;
    private TeleportRequestManager teleportRequestManager;
    private SeedAndShearBlocksHandler seedAndShearBlocksHandler;
    private SeenManager seenManager;
    private GodCommand godCommand;
    private AfkManager afkManager;
    private FlightManager flightManager;
    private ChatHandler chatHandler;

    @Override
    public void onEnable() {
        loadConfigurations();

        featureToggleManager = new FeatureToggleManager(this);

        // Initialize services
        protectionService = new ProtectionService(getServer().getPluginManager());
        sitService = new SitService(getServer().getPluginManager(), getLogger());
        territoryService = new TerritoryService(getServer().getPluginManager(), getLogger());

        // Initialize specific managers / handlers
        teleportRequestManager = new TeleportRequestManager(this);
        seedAndShearBlocksHandler = new SeedAndShearBlocksHandler(protectionService);

        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.SEEN_SYSTEM)) {
            seenManager = new SeenManager();
        }

        // Initialize CooldownManager singleton
        CooldownManager.getInstance();

        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);

        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.AFK_SYSTEM)) {
            afkManager = new AfkManager(this);
        }

        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.FLIGHT_SYSTEM)) {
            flightManager = new FlightManager(this, territoryService);
        }

        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.CHAT_MANAGEMENT)) {
            chatHandler = new ChatHandler(this);
        }

        doStartupOperations();
        registerPlaceholders();

        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.GOD_MODE)) {
            godCommand = new GodCommand();
        }

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

    public FeatureToggleManager getFeatureToggleManager() {
        return featureToggleManager;
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
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.CRAFTING_STATIONS)) {
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
        }
        // Admin Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.ADMIN_COMMANDS)) {
            BlockInfoCommand blockInfoCommand = new BlockInfoCommand();
            commandManager.registerCommand("blockinfo", blockInfoCommand, blockInfoCommand);
            EntityInfoCommand entityInfoCommand = new EntityInfoCommand();
            commandManager.registerCommand("entityinfo", entityInfoCommand, entityInfoCommand);
            ItemInfoCommand itemInfoCommand = new ItemInfoCommand();
            GamemodeCommand gmcCommand = new GamemodeCommand(GameMode.CREATIVE, "prismautils.command.gamemode.creative", "/gmc [player]");
            commandManager.registerCommand("gmc", gmcCommand, gmcCommand);
            GamemodeCommand gmsCommand = new GamemodeCommand(GameMode.SURVIVAL, "prismautils.command.gamemode.survival", "/gms [player]");
            commandManager.registerCommand("gms", gmsCommand, gmsCommand);
            GamemodeCommand gmaCommand = new GamemodeCommand(GameMode.ADVENTURE, "prismautils.command.gamemode.adventure", "/gma [player]");
            commandManager.registerCommand("gma", gmaCommand, gmaCommand);
            GamemodeCommand gmspCommand = new GamemodeCommand(GameMode.SPECTATOR, "prismautils.command.gamemode.spectator", "/gmsp [player]");
            commandManager.registerCommand("gmsp", gmspCommand, gmspCommand);
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
            CenterBlockCommand centerBlockCommand = new CenterBlockCommand();
            commandManager.registerCommand("centerblock", centerBlockCommand, centerBlockCommand);
            PatrolCommand patrolCommand = new PatrolCommand();
            commandManager.registerCommand("patrol", patrolCommand, patrolCommand);
            FeaturesCommand featuresCommand = new FeaturesCommand(this);
            commandManager.registerCommand("prismautilsfeatures", featuresCommand, featuresCommand);
        }
        // General Player Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.PLAYER_COMMANDS)) {
            BottomCommand bottomCommand = new BottomCommand();
            commandManager.registerCommand("bottom", bottomCommand, bottomCommand);
            RestoreHungerCommand restoreHungerCommand = new RestoreHungerCommand();
            commandManager.registerCommand("feed", restoreHungerCommand, restoreHungerCommand);
            FlySpeedCommand flySpeedCommand = new FlySpeedCommand();
            commandManager.registerCommand("flyspeed", flySpeedCommand, flySpeedCommand);
            RestoreHealthCommand restoreHealthCommand = new RestoreHealthCommand();
            commandManager.registerCommand("heal", restoreHealthCommand, restoreHealthCommand);
            pTimeCommand pTimeCommand = new pTimeCommand();
            commandManager.registerCommand("ptime", pTimeCommand, pTimeCommand);
            pWeatherCommand pWeatherCommand = new pWeatherCommand();
            commandManager.registerCommand("pweather", pWeatherCommand, pWeatherCommand);
            TopCommand topCommand = new TopCommand();
            commandManager.registerCommand("top", topCommand, topCommand);
        }
        // God Command
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.GOD_MODE) && godCommand != null) {
            commandManager.registerCommand("god", godCommand, godCommand);
        }
        // Homes Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.HOMES)) {
            HomesCommand homesCommand = new HomesCommand(this);
            commandManager.registerCommand("homes", homesCommand, homesCommand);
            commandManager.registerCommand("home", homesCommand, homesCommand);
            SetHomeCommand setHomeCommand = new SetHomeCommand(this, homesCommand);
            commandManager.registerCommand("sethome", setHomeCommand, setHomeCommand);
            DelHomeCommand delHomeCommand = new DelHomeCommand(this, homesCommand);
            commandManager.registerCommand("delhome", delHomeCommand, delHomeCommand);
            AdminHomesCommand adminHomesCommand = new AdminHomesCommand(this, homesCommand);
            commandManager.registerCommand("adminhome", adminHomesCommand, adminHomesCommand);
        }
        // Fly Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.FLIGHT_SYSTEM) && flightManager != null) {
            FlyCommand flyCommand = new FlyCommand(flightManager);
            commandManager.registerCommand("fly", flyCommand, flyCommand);
            FlyTimeCommand flyTimeCommand = new FlyTimeCommand(flightManager);
            commandManager.registerCommand("flytime", flyTimeCommand, flyTimeCommand);
            TempFlyCommand tempFlyCommand = new TempFlyCommand(flightManager);
            commandManager.registerCommand("tempfly", tempFlyCommand, tempFlyCommand);
        }
        // Teleport Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.TELEPORT_COMMANDS)) {
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
        }
        // Warp Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.WARPS)) {
            WarpsConfigManager warpsConfigManager = ConfigManager.getInstance().getWarpsConfig();
            WarpCommand warpCommand = new WarpCommand(warpsConfigManager, this);
            commandManager.registerCommand("warp", warpCommand, warpCommand);
            SetWarpCommand setWarpCommand = new SetWarpCommand(warpsConfigManager);
            commandManager.registerCommand("setwarp", setWarpCommand, setWarpCommand);
            DelWarpCommand delWarpCommand = new DelWarpCommand(warpsConfigManager);
            commandManager.registerCommand("delwarp", delWarpCommand, delWarpCommand);
        }
        // Teleport request commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.TELEPORT_REQUESTS)) {
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
        }
        // Afk Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.AFK_SYSTEM) && afkManager != null) {
            AfkCommand afkCommand = new AfkCommand(afkManager);
            commandManager.registerCommand("afk", afkCommand, afkCommand);
        }
        // Mail Command
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.MAIL_SYSTEM)) {
            MailCommand mailCommand = new MailCommand(this);
            commandManager.registerCommand("mail", mailCommand, mailCommand);
        }
        // Utility Commands
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.UTILITY_COMMANDS)) {
            ItemNameCommand itemNameCommand = new ItemNameCommand();
            commandManager.registerCommand("itemname", itemNameCommand, itemNameCommand);
            NearCommand nearCommand = new NearCommand();
            commandManager.registerCommand("near", nearCommand, nearCommand);
            PingCommand pingCommand = new PingCommand();
            commandManager.registerCommand("ping", pingCommand, pingCommand);
            RepairCommand repairCommand = new RepairCommand();
            commandManager.registerCommand("repair", repairCommand, repairCommand);
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
            PlayTimeCommand playTimeCommand = new PlayTimeCommand();
            commandManager.registerCommand("playtime", playTimeCommand, playTimeCommand);
        }
        // Seen Command
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.SEEN_SYSTEM) && seenManager != null) {
            SeenCommand seenCommand = new SeenCommand(seenManager);
            commandManager.registerCommand("seen", seenCommand, seenCommand);
        }
    }

    private void registerEvents(EventManager eventManager) {
        // Always register player data handler
        eventManager.registerListeners(new PlayerDataHandler(playerDataManager));

        // Register feature-toggled listeners
        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.REMOVE_EXCESS_DROPS,
                new RemoveDropsHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SLIME_SPLIT_CONTROL,
                new SlimeSplitHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.CUSTOM_DEATH_MESSAGES,
                new CustomDeathMessageHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.HEALTH_SCALE,
                new HealthScaleHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.FIRST_JOIN_COMMANDS,
                new FirstJoinCommandsHandler(this));

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.FIRST_JOIN_SPAWN,
                new FirstJoinSpawnHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SILK_SPAWNERS,
                new SilkSpawnerHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SAFE_SPAWN_EGG,
                new SafeSpawnEggHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SEED_SHEAR_BLOCKS,
                seedAndShearBlocksHandler);

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.COPPER_OXIDATION_CONTROL,
                new CopperOxidationHandler(protectionService));

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.HORSE_ZOMBIFICATION,
                new HorseZombificationHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.PERMISSION_KEEP_INVENTORY,
                new PermissionKeepInvHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.RESPAWN_MESSAGE,
                new RespawnMessageHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.LAST_LOCATION_TRACKING,
                new LastLocationHandler(playerDataManager));

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.ANTI_AUTO_FISHING,
                new AntiAutoFishingHandler(this));

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SPAWNER_MOB_DROPS,
                new SpawnerMobItemDropsHandler(this));

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.ENDERMITE_LIGHTNING_IMMUNITY,
                new EntityLightningImmunityHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.CLIMBABLE_CHAINS,
                new ClimbableChainsHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.NON_LEVEL_ENCHANTING,
                new AlternativeEnchantingCostHandler());

        eventManager.registerFeatureListeners(FeatureToggleManager.Feature.NETHER_MOB_ZOMBIFICATION,
                new NetherMobZombificationHandler());

        // Listeners with null checks
        if (chatHandler != null) {
            eventManager.registerFeatureListeners(FeatureToggleManager.Feature.CHAT_MANAGEMENT, chatHandler);
        }

        if (afkManager != null && ConfigManager.getInstance().getAfkConfig().afkEnabled) {
            eventManager.registerFeatureListeners(FeatureToggleManager.Feature.AFK_SYSTEM,
                    new AfkProtectionHandler(afkManager, ConfigManager.getInstance().getAfkConfig()),
                    afkManager);
        }

        if (seenManager != null) {
            eventManager.registerFeatureListeners(FeatureToggleManager.Feature.SEEN_SYSTEM,
                    new SeenEventsHandler(seenManager));
        }

        if (godCommand != null) {
            eventManager.registerFeatureListeners(FeatureToggleManager.Feature.GOD_MODE, godCommand);
        }
    }

    private void doStartupOperations() {
        if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.DISABLED_RECIPES)) {
            new DisabledCraftingRecipesManager().removeConfiguredRecipes();
        }
    }

    private void registerPlaceholders() {
        if (!featureToggleManager.isEnabled(FeatureToggleManager.Feature.PLACEHOLDERAPI)) {
            return;
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MiniMessagePlaceholderExpansion().register();
            new UnixLocalTimeExpansion().register();
            if (featureToggleManager.isEnabled(FeatureToggleManager.Feature.AFK_SYSTEM) && afkManager != null) {
                new AfkPlaceholderExpansion(afkManager).register();
            }
            new EventPlaceholderExpansion().register();
            new TimePlaceholderExpansion().register();
        } else {
            getLogger().warning("PlaceholderAPI doesn't exist! Placeholders will not work.");
        }
    }

    private void startTasks() {
        new AfkTitlesHandler().runTaskTimer(this, 0, 80);
    }

    public SitService getSitService() {
        return sitService;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }
}
