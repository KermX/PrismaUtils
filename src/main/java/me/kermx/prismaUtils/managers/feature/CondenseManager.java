package me.kermx.prismaUtils.managers.feature;

import me.kermx.prismaUtils.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class CondenseManager {

    public enum Mode {
        ALL,
        REVERSIBLE_ONLY
    }

    public record Conversion(Material from, Material to, int removed, int given) {}

    private final CondenseMaterialsManager cmm;

    public CondenseManager(CondenseMaterialsManager cmm) {
        this.cmm = Objects.requireNonNull(cmm, "CondenseMaterialsManager cannot be null");
    }

    public List<Conversion> condense(Player player, Mode mode, Material specificInputOrNull) {
        Objects.requireNonNull(player, "Player cannot be null");

        Map<Material, Integer> recipes = (mode == Mode.REVERSIBLE_ONLY) ? cmm.getReversibleRecipes() : cmm.getRecipes();
        PlayerInventory inv = player.getInventory();

        Map<Material, Integer> snapshot = snapshotPlainCounts(inv);

        List<Conversion> conversions = new ArrayList<>();
        if (specificInputOrNull != null) {
            computeCondenseForMaterial(snapshot, conversions, specificInputOrNull, recipes);
        } else {
            for (Material in : recipes.keySet()) {
                computeCondenseForMaterial(snapshot, conversions, in, recipes);
            }
        }

        applyConversionsCondense(player, conversions);
        return conversions;
    }

    public List<Conversion> uncondense(Player player, Material specificBlockOrNull) {
        Objects.requireNonNull(player, "Player cannot be null");

        // For uncondense we only support reversible mappings.
        Map<Material, Material> baseToBlock = cmm.getReversibleMaterialMappings(false);
        Map<Material, Integer> baseToAmount = cmm.getReversibleRecipes();

        PlayerInventory inv = player.getInventory();
        Map<Material, Integer> snapshot = snapshotPlainCounts(inv);

        // Build rules of: block -> base (x amount)
        List<Conversion> conversions = new ArrayList<>();

        if (specificBlockOrNull != null) {
            computeUncondenseForBlock(snapshot, conversions, specificBlockOrNull, baseToBlock, baseToAmount);
        } else {
            for (Material base : baseToBlock.keySet()) {
                Material block = baseToBlock.get(base);
                computeUncondenseForBlock(snapshot, conversions, block, baseToBlock, baseToAmount);
            }
        }

        applyConversionsUncondense(player, conversions);
        return conversions;
    }

    private static Map<Material, Integer> snapshotPlainCounts(PlayerInventory inv) {
        Map<Material, Integer> counts = new EnumMap<>(Material.class);
        for (ItemStack stack : inv.getStorageContents()) {
            if (stack == null) continue;
            if (stack.getType().isAir()) continue;
            if (ItemUtils.itemHasSpecialMeta(stack)) continue;

            counts.merge(stack.getType(), stack.getAmount(), Integer::sum);
        }
        return counts;
    }

    private void computeCondenseForMaterial(
            Map<Material, Integer> snapshot,
            List<Conversion> out,
            Material input,
            Map<Material, Integer> recipes
    ) {
        Integer inputPer = recipes.get(input);
        if (inputPer == null || inputPer <= 1) return;

        Material result = cmm.getResultMaterial(input, false);
        if (result == null || result.isAir()) return;

        int available = snapshot.getOrDefault(input, 0);
        int blocks = available / inputPer;
        if (blocks <= 0) return;

        int remove = blocks * inputPer;
        out.add(new Conversion(input, result, remove, blocks));
    }

    private void computeUncondenseForBlock(
            Map<Material, Integer> snapshot,
            List<Conversion> out,
            Material block,
            Map<Material, Material> baseToBlock,
            Map<Material, Integer> baseToAmount
    ) {
        Material base = null;
        for (Map.Entry<Material, Material> e : baseToBlock.entrySet()) {
            if (e.getValue() == block) {
                base = e.getKey();
                break;
            }
        }
        if (base == null) return;

        Integer outPerBlock = baseToAmount.get(base);
        if (outPerBlock == null || outPerBlock <= 0) return;

        int availableBlocks = snapshot.getOrDefault(block, 0);
        if (availableBlocks <= 0) return;

        int give = availableBlocks * outPerBlock;
        out.add(new Conversion(block, base, availableBlocks, give));
    }

    private void applyConversionsCondense(Player player, List<Conversion> conversions) {
        PlayerInventory inv = player.getInventory();

        for (Conversion c : conversions) {
            int removed = ItemUtils.removePlainItems(inv, c.from(), c.removed());
            if (removed <= 0) continue;

            // Because we remove exactly "blocks * inputPer", removed should always match plan,
            // but if it doesn't, recompute outputs proportionally and stay safe.
            int inputPer = cmm.getRecipes().getOrDefault(c.from(), 1);
            int blocks = (inputPer > 0) ? (removed / inputPer) : 0;
            if (blocks <= 0) continue;

            ItemUtils.giveItems(player, c.to(), blocks);

            // Give-back empties are based on how many inputs were actually consumed
            Material empty = cmm.getGiveBackEmptyMappings().get(c.from());
            if (empty != null) {
                ItemUtils.giveItems(player, empty, blocks * inputPer);
            }
        }
    }

    private static void applyConversionsUncondense(Player player, List<Conversion> conversions) {
        PlayerInventory inv = player.getInventory();

        for (Conversion c : conversions) {
            int removed = ItemUtils.removePlainItems(inv, c.from(), c.removed());
            if (removed <= 0) continue;

            int outPer = (c.removed() == 0) ? 0 : (c.given() / c.removed());
            int give = removed * outPer;
            if (give <= 0) continue;

            int max = c.to().getMaxStackSize();
            int remaining = give;
            while (remaining > 0) {
                int stack = Math.min(max, remaining);
                ItemUtils.giveItems(player, new ItemStack(c.to(), stack));
                remaining -= stack;
            }
        }
    }
}
