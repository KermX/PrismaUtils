package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class LightLevelCommand extends BaseCommand {

    // Store active visualizations by player
    private static final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    // Configuration parameters
    private static final int DEFAULT_RADIUS = 15;
    private static final int DEFAULT_DURATION = 30; // seconds
    private static final int MAX_RADIUS = 30;
    private static final int PARTICLES_PER_BLOCK = 3;

    // Permission nodes
    private static final String CONFIG_PERMISSION = "prismautils.command.lightlevel.config";

    // Store plugin instance
    private final PrismaUtils plugin;

    //TODO: make messages config-able

    public LightLevelCommand() {
        super("prismautils.command.lightlevel", false, "/lightlevel [radius] [duration]");
        this.plugin = JavaPlugin.getPlugin(PrismaUtils.class);
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        // Parse arguments - only if player has config permission
        int radius = DEFAULT_RADIUS;
        int duration = DEFAULT_DURATION;

        // Check if player has configuration permission
        boolean hasConfigPermission = player.hasPermission(CONFIG_PERMISSION);

        if (hasConfigPermission && args.length >= 1) {
            try {
                radius = Integer.parseInt(args[0]);
                if (radius <= 0) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<red>Radius must be greater than 0."));
                    return true;
                }
                if (radius > MAX_RADIUS) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<red>Maximum radius is " + MAX_RADIUS + "."));
                    radius = MAX_RADIUS;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Invalid radius. Please enter a number."));
                return true;
            }
        }

        if (hasConfigPermission && args.length >= 2) {
            try {
                duration = Integer.parseInt(args[1]);
                if (duration <= 0) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<red>Duration must be greater than 0."));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Invalid duration. Please enter a number."));
                return true;
            }
        }

        // If player specified arguments but doesn't have permission
        if (!hasConfigPermission && args.length > 0) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have permission to configure this command."));
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Using default values: radius=" + DEFAULT_RADIUS + ", duration=" + DEFAULT_DURATION + " seconds."));
            // Continue with default values
        }

        // Cancel any existing task for this player
        cancelExistingTask(player.getUniqueId());

        // Start new visualization task
        startVisualization(player, radius, duration);

        player.sendMessage(TextUtils.deserializeString(
                "<red>Showing light levels within " + radius +
                        " blocks for " + duration + " seconds."));
        player.sendMessage(TextUtils.deserializeString(
                "<red><bold>Red particles<reset>: Light level 0 (hostile mobs can spawn)"));
        player.sendMessage(TextUtils.deserializeString(
                "<yellow><bold>Yellow particles<reset>: Light level 1-7 (spawn possible in some conditions)"));
        player.sendMessage(TextUtils.deserializeString(
                "<green><bold>Green particles<reset>: Light level 8+ (safe from hostile mob spawning)"));

        return true;
    }

    private void cancelExistingTask(UUID playerId) {
        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }
    }

    private void startVisualization(Player player, int radius, int duration) {
        UUID playerId = player.getUniqueId();

        BukkitTask task = new BukkitRunnable() {
            private int timeLeft = duration;

            @Override
            public void run() {
                if (timeLeft <= 0 || !player.isOnline()) {
                    this.cancel();
                    activeTasks.remove(playerId);
                    if (player.isOnline()) {
                        player.sendMessage(TextUtils.deserializeString(
                                "<green>Light level visualization ended."));
                    }
                    return;
                }

                visualizeSpawnableBlocks(player, radius);
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeTasks.put(playerId, task);
    }

    private void visualizeSpawnableBlocks(Player player, int radius) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();

        int playerX = playerLoc.getBlockX();
        int playerY = playerLoc.getBlockY();
        int playerZ = playerLoc.getBlockZ();

        for (int x = playerX - radius; x <= playerX + radius; x++) {
            for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                // Check blocks from Y-5 to Y+5
                for (int y = Math.max(world.getMinHeight(), playerY - 5);
                     y <= Math.min(world.getMaxHeight() - 1, playerY + 5);
                     y++) {

                    // Skip blocks too far away (spherical radius check)
                    if (Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2) + Math.pow(z - playerZ, 2)) > radius) {
                        continue;
                    }

                    Block block = world.getBlockAt(x, y, z);
                    Block blockAbove = world.getBlockAt(x, y + 1, z);

                    // Check if the block could potentially support mob spawning
                    if (isSpawnableBlock(block, blockAbove)) {
                        int lightLevel = blockAbove.getLightLevel();

                        // Spawn particles based on light level
                        Location particleLoc = new Location(world, x + 0.5, y + 1.1, z + 0.5);

                        if (lightLevel == 0) {
                            // Red particles for light level 0 (high spawn risk)
                            spawnColoredParticles(player, particleLoc, 255, 0, 0); // Red
                        } else if (lightLevel <= 7) {
                            // Yellow particles for light level 1-7 (some spawn risk)
                            spawnColoredParticles(player, particleLoc, 255, 255, 0); // Yellow
                        } else {
                            // Green particles for light level 8+ (safe)
                            spawnColoredParticles(player, particleLoc, 0, 255, 0); // Green
                        }
                    }
                }
            }
        }
    }

    private void spawnColoredParticles(Player player, Location location, int red, int green, int blue) {
        // Spread particles slightly for better visibility
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), 1.0f);
        for (int i = 0; i < PARTICLES_PER_BLOCK; i++) {
            player.spawnParticle(
                    Particle.DUST,
                    location.getX() + (Math.random() - 0.5) * 0.8,
                    location.getY() + (Math.random() - 0.5) * 0.2,
                    location.getZ() + (Math.random() - 0.5) * 0.8,
                    0, dustOptions
            );
        }
    }

    private boolean isSpawnableBlock(Block block, Block blockAbove) {
        // Check if the block is a valid spawning block for mobs
        return block.getType().isSolid() &&
                !block.getType().isTransparent() &&
                blockAbove.getType().isTransparent() &&
                blockAbove.getType() != Material.WATER &&
                blockAbove.getType() != Material.LAVA;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // Only offer tab completions to players with config permission
        if (!(sender instanceof Player) || !sender.hasPermission(CONFIG_PERMISSION)) {
            return completions;
        }

        if (args.length == 1) {
            // Suggest default radius and other values
            completions.add(String.valueOf(DEFAULT_RADIUS));
            completions.add("5");
            completions.add("10");
            completions.add("20");
        } else if (args.length == 2) {
            // Suggest default duration and other values
            completions.add(String.valueOf(DEFAULT_DURATION));
            completions.add("10");
            completions.add("60");
            completions.add("120");
        }

        return completions;
    }
}
