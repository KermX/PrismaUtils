package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class LimitsCommand extends BaseCommand {

    private final PrismaUtils plugin;

    //TODO: make messages config-able
    //TODO: do something with this command, it doesn't really
    //TODO: use textutils methods properly

    public LimitsCommand(PrismaUtils plugin) {
        super("prismautils.command.limits", false, "/limits");
        this.plugin = JavaPlugin.getPlugin(PrismaUtils.class);
    }

    private static final Set<Material> ALLOWED_BLOCK_TYPES = EnumSet.of(
            Material.HOPPER, Material.PISTON, Material.STICKY_PISTON,
            Material.OBSERVER, Material.SPAWNER, Material.SCULK_SENSOR,
            Material.CALIBRATED_SCULK_SENSOR
    );

    private static final Set<EntityType> ALLOWED_ENTITY_TYPES = EnumSet.of(
            EntityType.ALLAY, EntityType.ARMADILLO, EntityType.ARROW, EntityType.AXOLOTL, EntityType.BAT,
            EntityType.BEE, EntityType.BLAZE, EntityType.BOGGED, EntityType.BREEZE, EntityType.CAMEL,
            EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CHICKEN, EntityType.COD, EntityType.COW,
            EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.DROWNED, EntityType.EGG,
            EntityType.ELDER_GUARDIAN, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON,
            EntityType.ENDER_PEARL, EntityType.EVOKER, EntityType.FIREBALL, EntityType.GIANT, EntityType.FOX,
            EntityType.FROG, EntityType.GHAST, EntityType.GLOW_SQUID, EntityType.GOAT, EntityType.GUARDIAN,
            EntityType.HOGLIN, EntityType.HORSE, EntityType.HUSK, EntityType.ILLUSIONER, EntityType.IRON_GOLEM,
            EntityType.LLAMA, EntityType.MAGMA_CUBE, EntityType.MOOSHROOM, EntityType.MULE, EntityType.OCELOT,
            EntityType.PANDA, EntityType.PARROT, EntityType.PHANTOM, EntityType.PIG, EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.POLAR_BEAR, EntityType.PUFFERFISH,
            EntityType.RABBIT, EntityType.RAVAGER, EntityType.SALMON, EntityType.SHEEP, EntityType.SHULKER,
            EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.SLIME,
            EntityType.SNIFFER, EntityType.SNOWBALL, EntityType.SNOW_GOLEM, EntityType.SPIDER, EntityType.SQUID,
            EntityType.STRAY, EntityType.STRIDER, EntityType.TADPOLE, EntityType.TRADER_LLAMA, EntityType.TROPICAL_FISH,
            EntityType.TURTLE, EntityType.VEX, EntityType.VILLAGER, EntityType.VINDICATOR, EntityType.WANDERING_TRADER,
            EntityType.WARDEN, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.WOLF,
            EntityType.ZOGLIN, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.MINECART, EntityType.HOPPER_MINECART, EntityType.CHEST_MINECART, EntityType.CREAKING, EntityType.HAPPY_GHAST
    );

    // Cache for max counts to avoid repeated calculations
    private static final Map<Material, Integer> MAX_BLOCK_COUNTS = new EnumMap<>(Material.class);
    private static final Map<EntityType, Integer> MAX_ENTITY_COUNTS = new EnumMap<>(EntityType.class);

    // Static initializer to populate max count caches
    static {
        // Populate block count limits
        for (Material material : ALLOWED_BLOCK_TYPES) {
            MAX_BLOCK_COUNTS.put(material, calculateMaxBlockCount(material));
        }

        // Populate entity count limits
        for (EntityType entityType : ALLOWED_ENTITY_TYPES) {
            MAX_ENTITY_COUNTS.put(entityType, calculateMaxEntityCount(entityType));
        }
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players");
            return true;
        }

        // Display a message to the player that scan is starting
        player.sendMessage(Component.text("Scanning chunk for limits...").color(NamedTextColor.YELLOW));

        // Get player's current chunk
        Block playerBlock = player.getLocation().getBlock();
        Chunk chunk = playerBlock.getChunk();
        String chunkCoordinates = "X: " + chunk.getX() + " Z: " + chunk.getZ();

        // Run the chunk scanning on the main thread but make it efficient
        Map<EntityType, Integer> entityCounts = countEntitiesInChunk(chunk);
        Map<Material, Integer> blockCounts = countBlocksInChunk(chunk);

        // Send results to player
        player.sendMessage(Component.text("Scanned Chunk: " + chunkCoordinates).color(NamedTextColor.GREEN));
        sendCountMessage(player, blockCounts, "Blocks");
        sendCountMessage(player, entityCounts, "Entities");

        return true;
    }

    private <T> void sendCountMessage(Player player, Map<T, Integer> counts, String countType) {
        player.sendMessage(Component.text("----- " + countType + " -----").color(TextColor.color(0x00AAFF))
                .decorate(TextDecoration.BOLD));

        if (counts.isEmpty()) {
            player.sendMessage(Component.text("No " + countType.toLowerCase() + " found in this chunk.").color(NamedTextColor.GRAY));
            return;
        }

        // Process only non-zero counts for efficiency
        counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Object::toString)))
                .forEach(entry -> {
                    T type = entry.getKey();
                    int count = entry.getValue();
                    int maxCount;

                    if (type instanceof Material material) {
                        maxCount = MAX_BLOCK_COUNTS.get(material);
                    } else if (type instanceof EntityType entityType) {
                        maxCount = MAX_ENTITY_COUNTS.get(entityType);
                    } else {
                        maxCount = 0;
                    }

                    String name = formatName(type.toString());

                    if (maxCount > 0) {
                        double percentage = ((double) count / maxCount) * 100;
                        TextColor color = getColorByPercentage(percentage);

                        String formattedMessage = String.format("%s: %d/%d (%.1f%%)",
                                name, count, maxCount, percentage);

                        player.sendMessage(Component.text(formattedMessage).color(color));
                    } else {
                        // For types with no max limit
                        player.sendMessage(Component.text(name + ": " + count).color(NamedTextColor.GREEN));
                    }
                });
    }

    private TextColor getColorByPercentage(double percentage) {
        if (percentage >= 90) {
            return NamedTextColor.RED; // Critical - over limit
        } else if (percentage >= 80) {
            return NamedTextColor.GOLD; // Warning - approaching limit
        } else if (percentage >= 60) {
            return NamedTextColor.YELLOW; // Caution - halfway to limit
        } else {
            return NamedTextColor.GREEN; // Good - well below limit
        }
    }


    private String formatName(String name) {
        name = name.replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }


    private Map<Material, Integer> countBlocksInChunk(Chunk chunk) {
        Map<Material, Integer> blockCounts = new EnumMap<>(Material.class);

        // Initialize the map with allowed types to avoid null checks later
        for (Material type : ALLOWED_BLOCK_TYPES) {
            blockCounts.put(type, 0);
        }

        // Use chunk snapshot instead of direct block access
        // This provides better performance by avoiding synchronous chunk loading
        ChunkSnapshot snapshot = chunk.getChunkSnapshot();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                    Material type = snapshot.getBlockType(x, y, z);
                    if (ALLOWED_BLOCK_TYPES.contains(type)) {
                        blockCounts.put(type, blockCounts.get(type) + 1);
                    }
                }
            }
        }

        // Remove entries with zero counts to reduce message size
        blockCounts.entrySet().removeIf(entry -> entry.getValue() == 0);

        return blockCounts;
    }

    private Map<EntityType, Integer> countEntitiesInChunk(Chunk chunk) {
        Map<EntityType, Integer> entityCounts = new EnumMap<>(EntityType.class);

        // Pre-populate with allowed types
        for (EntityType type : ALLOWED_ENTITY_TYPES) {
            entityCounts.put(type, 0);
        }

        // Get all entities in one call
        for (Entity entity : chunk.getEntities()) {
            EntityType type = entity.getType();
            if (ALLOWED_ENTITY_TYPES.contains(type)) {
                entityCounts.compute(type, (key, value) -> value + 1);
            }
        }

        // Remove entries with zero counts to reduce message size
        entityCounts.entrySet().removeIf(entry -> entry.getValue() == 0);

        return entityCounts;
    }

    // Static methods to calculate max counts
    private static int calculateMaxBlockCount(Material material) {
        return switch (material) {
            case HOPPER -> 96;
            case PISTON, STICKY_PISTON -> 128;
            case OBSERVER -> 64;
            case SPAWNER -> 5;
            case SCULK_SENSOR, CALIBRATED_SCULK_SENSOR -> 12;
            default -> 0;
        };
    }

    private static int calculateMaxEntityCount(EntityType entityType) {
        // Return appropriate limits for entity types
        // This is a placeholder - replace with actual logic
        return switch (entityType) {
            case ALLAY, MINECART, HOPPER_MINECART, CHEST_MINECART -> 8;

            // Entities with limit 16
            case ARROW, EGG, GIANT, ENDER_PEARL, SNOWBALL, SPECTRAL_ARROW, FIREBALL, COW, PIG,
                 CHICKEN, SHEEP, GOAT, LLAMA, MOOSHROOM -> 16;

            case BEE -> 24;

            // Entity with limit 5
            case ENDER_DRAGON -> 5;

            // All remaining entities use a limit of 32
            case ARMADILLO, AXOLOTL, BAT, BLAZE, BOGGED, BREEZE, CAMEL, CAT, CAVE_SPIDER,
                 COD, CREEPER, DOLPHIN, DONKEY, DROWNED, ELDER_GUARDIAN, ENDERMAN,
                 ENDERMITE, EVOKER, FOX, FROG, GHAST, GLOW_SQUID, GUARDIAN, HOGLIN, HORSE,
                 HUSK, ILLUSIONER, IRON_GOLEM, MAGMA_CUBE, MULE, OCELOT, PANDA,
                 PARROT, PHANTOM, PIGLIN, PIGLIN_BRUTE, PILLAGER, POLAR_BEAR, PUFFERFISH,
                 RABBIT, RAVAGER, SALMON, SHULKER, SILVERFISH, SKELETON, SKELETON_HORSE,
                 SLIME, SNIFFER, SNOW_GOLEM, SPIDER, SQUID, STRAY, STRIDER, TADPOLE, TRADER_LLAMA,
                 TROPICAL_FISH, TURTLE, VEX, VILLAGER, VINDICATOR, WANDERING_TRADER, WARDEN,
                 WITCH, WITHER, WITHER_SKELETON, WOLF, ZOGLIN, ZOMBIE, ZOMBIE_HORSE, ZOMBIE_VILLAGER,
                 ZOMBIFIED_PIGLIN, CREAKING -> 32;
            default -> 32; // Default limit for other entities
        };
    }

    // Use the cached values for performance
    private int getMaxCountForBlockType(Material material) {
        return MAX_BLOCK_COUNTS.getOrDefault(material, 0);
    }

    private int getMaxCountForEntityType(EntityType entityType) {
        return MAX_ENTITY_COUNTS.getOrDefault(entityType, 0);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion options as the command doesn't take arguments
        return Collections.emptyList();
    }
}

