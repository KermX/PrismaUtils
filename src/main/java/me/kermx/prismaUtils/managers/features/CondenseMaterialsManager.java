package me.kermx.prismaUtils.managers.features;

import me.kermx.prismaUtils.utils.GenUtils;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CondenseMaterialsManager {
    // Use final maps to hold constant mappings.
    private final Map<Material, Integer> irreversibleRecipes;
    private final Map<Material, Integer> reversibleRecipes;
    private final Map<Material, Integer> recipes;
    private final Map<Material, Material> giveBackEmptyMappings;
    private final Map<Material, Material> irreversibleMaterialMappings;
    private final Map<Material, Material> reversibleMaterialMappings;
    private final Map<Material, Material> materialMappings;

    public CondenseMaterialsManager() {
        // Initialize irreversible recipes.
        irreversibleRecipes = new HashMap<>();
        irreversibleRecipes.put(Material.QUARTZ, 4);
        irreversibleRecipes.put(Material.AMETHYST_SHARD, 4);
        irreversibleRecipes.put(Material.HONEY_BOTTLE, 4);
        irreversibleRecipes.put(Material.HONEYCOMB, 4);
        irreversibleRecipes.put(Material.PRISMARINE_SHARD, 4);
        irreversibleRecipes.put(Material.BRICK, 4);
        irreversibleRecipes.put(Material.NETHER_BRICK, 4);
        irreversibleRecipes.put(Material.NETHER_WART, 9);
        irreversibleRecipes.put(Material.GLOWSTONE_DUST, 4);
        irreversibleRecipes.put(Material.STRING, 4);
        irreversibleRecipes.put(Material.SNOWBALL, 4);
        irreversibleRecipes.put(Material.BAMBOO, 9);
        irreversibleRecipes.put(Material.PACKED_ICE, 9);
        irreversibleRecipes.put(Material.ICE, 9);
        irreversibleRecipes.put(Material.POPPED_CHORUS_FRUIT, 4);
        irreversibleRecipes.put(Material.CHARCOAL, 9);
        irreversibleRecipes.put(Material.MAGMA_CREAM, 4);

        // Initialize reversible recipes.
        reversibleRecipes = new HashMap<>();
        reversibleRecipes.put(Material.BONE_MEAL, 9);
        reversibleRecipes.put(Material.WHEAT, 9);
        reversibleRecipes.put(Material.COAL, 9);
        reversibleRecipes.put(Material.RAW_IRON, 9);
        reversibleRecipes.put(Material.RAW_COPPER, 9);
        reversibleRecipes.put(Material.RAW_GOLD, 9);
        reversibleRecipes.put(Material.EMERALD, 9);
        reversibleRecipes.put(Material.LAPIS_LAZULI, 9);
        reversibleRecipes.put(Material.DIAMOND, 9);
        reversibleRecipes.put(Material.IRON_NUGGET, 9);
        reversibleRecipes.put(Material.GOLD_NUGGET, 9);
        reversibleRecipes.put(Material.IRON_INGOT, 9);
        reversibleRecipes.put(Material.GOLD_INGOT, 9);
        reversibleRecipes.put(Material.COPPER_INGOT, 9);
        reversibleRecipes.put(Material.SLIME_BALL, 9);
        reversibleRecipes.put(Material.NETHERITE_INGOT, 9);
        reversibleRecipes.put(Material.REDSTONE, 9);
        reversibleRecipes.put(Material.MELON_SLICE, 9);
        reversibleRecipes.put(Material.DRIED_KELP, 9);
        reversibleRecipes.put(Material.CLAY_BALL, 4);

        // Combine irreversible and reversible recipes.
        recipes = new HashMap<>();
        recipes.putAll(irreversibleRecipes);
        recipes.putAll(reversibleRecipes);

        // Mapping for items that return an "empty" item.
        giveBackEmptyMappings = new HashMap<>();
        giveBackEmptyMappings.put(Material.HONEY_BOTTLE, Material.GLASS_BOTTLE);

        // Initialize irreversible material mappings.
        irreversibleMaterialMappings = new HashMap<>();
        irreversibleMaterialMappings.put(Material.QUARTZ, Material.QUARTZ_BLOCK);
        irreversibleMaterialMappings.put(Material.HONEYCOMB, Material.HONEYCOMB_BLOCK);
        irreversibleMaterialMappings.put(Material.HONEY_BOTTLE, Material.HONEY_BLOCK);
        irreversibleMaterialMappings.put(Material.CHARCOAL, Material.COAL_BLOCK);
        irreversibleMaterialMappings.put(Material.AMETHYST_SHARD, Material.AMETHYST_BLOCK);
        irreversibleMaterialMappings.put(Material.PRISMARINE_SHARD, Material.PRISMARINE);
        irreversibleMaterialMappings.put(Material.GLOWSTONE_DUST, Material.GLOWSTONE);
        irreversibleMaterialMappings.put(Material.NETHER_WART, Material.NETHER_WART_BLOCK);
        irreversibleMaterialMappings.put(Material.NETHER_BRICK, Material.NETHER_BRICKS);
        irreversibleMaterialMappings.put(Material.BRICK, Material.BRICKS);
        irreversibleMaterialMappings.put(Material.SNOWBALL, Material.SNOW_BLOCK);
        irreversibleMaterialMappings.put(Material.STRING, Material.WHITE_WOOL);
        irreversibleMaterialMappings.put(Material.BAMBOO, Material.BAMBOO_BLOCK);
        irreversibleMaterialMappings.put(Material.PACKED_ICE, Material.BLUE_ICE);
        irreversibleMaterialMappings.put(Material.ICE, Material.PACKED_ICE);
        irreversibleMaterialMappings.put(Material.POPPED_CHORUS_FRUIT, Material.PURPUR_BLOCK);
        irreversibleMaterialMappings.put(Material.MAGMA_CREAM, Material.MAGMA_BLOCK);

        // Initialize reversible material mappings.
        reversibleMaterialMappings = new HashMap<>();
        reversibleMaterialMappings.put(Material.COAL, Material.COAL_BLOCK);
        reversibleMaterialMappings.put(Material.RAW_GOLD, Material.RAW_GOLD_BLOCK);
        reversibleMaterialMappings.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
        reversibleMaterialMappings.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        reversibleMaterialMappings.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        reversibleMaterialMappings.put(Material.IRON_NUGGET, Material.IRON_INGOT);
        reversibleMaterialMappings.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        reversibleMaterialMappings.put(Material.SLIME_BALL, Material.SLIME_BLOCK);
        reversibleMaterialMappings.put(Material.RAW_COPPER, Material.RAW_COPPER_BLOCK);
        reversibleMaterialMappings.put(Material.RAW_IRON, Material.RAW_IRON_BLOCK);
        reversibleMaterialMappings.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        reversibleMaterialMappings.put(Material.EMERALD, Material.EMERALD_BLOCK);
        reversibleMaterialMappings.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        reversibleMaterialMappings.put(Material.GOLD_NUGGET, Material.GOLD_INGOT);
        reversibleMaterialMappings.put(Material.WHEAT, Material.HAY_BLOCK);
        reversibleMaterialMappings.put(Material.BONE_MEAL, Material.BONE_BLOCK);
        reversibleMaterialMappings.put(Material.DRIED_KELP, Material.DRIED_KELP_BLOCK);
        reversibleMaterialMappings.put(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK);
        reversibleMaterialMappings.put(Material.MELON_SLICE, Material.MELON);
        reversibleMaterialMappings.put(Material.CLAY_BALL, Material.CLAY);

        // Combine both irreversible and reversible material mappings.
        materialMappings = new HashMap<>();
        materialMappings.putAll(irreversibleMaterialMappings);
        materialMappings.putAll(reversibleMaterialMappings);
    }

    public Map<Material, Integer> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }

    public Map<Material, Integer> getIrreversibleRecipes() {
        return Collections.unmodifiableMap(irreversibleRecipes);
    }

    public Map<Material, Integer> getReversibleRecipes() {
        return Collections.unmodifiableMap(reversibleRecipes);
    }

    public Map<Material, Material> getGiveBackEmptyMappings() {
        return Collections.unmodifiableMap(giveBackEmptyMappings);
    }

    public Map<Material, Material> getMaterialMappings() {
        return Collections.unmodifiableMap(materialMappings);
    }

    public Map<Material, Material> getIrreversibleMaterialMappings() {
        return Collections.unmodifiableMap(irreversibleMaterialMappings);
    }

    /**
     * Returns either the reversible material mapping or its inverse (for "uncondensing")
     * depending on the flag.
     */
    public Map<Material, Material> getReversibleMaterialMappings(boolean uncondense) {
        if (uncondense) {
            return Collections.unmodifiableMap(GenUtils.invertMap(reversibleMaterialMappings));
        }
        return Collections.unmodifiableMap(reversibleMaterialMappings);
    }

    /**
     * Given an input material, returns the corresponding result material.
     *
     * @param inputMaterial the material to condense
     * @param uncondense whether to get the inverse mapping (not implemented)
     * @return the resulting material, or AIR if none is defined
     */
    public Material getResultMaterial(Material inputMaterial, boolean uncondense) {
        if (uncondense) {
            // Implement uncondensing if needed.
            throw new UnsupportedOperationException("Uncondense is not implemented yet.");
        }
        return materialMappings.getOrDefault(inputMaterial, Material.AIR);
    }
}
