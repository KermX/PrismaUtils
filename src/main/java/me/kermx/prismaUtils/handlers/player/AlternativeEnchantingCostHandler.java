package me.kermx.prismaUtils.handlers.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class AlternativeEnchantingCostHandler implements Listener {

    // Level thresholds for discount tiers
    private static final int NO_DISCOUNT_THRESHOLD = 30;
    private static final int TIER1_THRESHOLD = 250;
    private static final int TIER2_THRESHOLD = 500;
    private static final int MAX_DISCOUNT_THRESHOLD = 1000;

    // Scale factors for each tier (percentage of original cost)
    private static final double BASE_SCALE_FACTOR = 1.0;     // 100% of cost
    private static final double TIER1_SCALE_FACTOR = 0.8;    // 80% of cost
    private static final double TIER2_SCALE_FACTOR = 0.6;    // 60% of cost
    private static final double MAX_DISCOUNT_FACTOR = 0.4;   // 40% of cost (60% discount)

    // Discount rates per level for each tier
    private static final double TIER1_DISCOUNT_RATE = (BASE_SCALE_FACTOR - TIER1_SCALE_FACTOR) /
            (TIER1_THRESHOLD - NO_DISCOUNT_THRESHOLD);
    private static final double TIER2_DISCOUNT_RATE = (TIER1_SCALE_FACTOR - TIER2_SCALE_FACTOR) /
            (TIER2_THRESHOLD - TIER1_THRESHOLD);
    private static final double TIER3_DISCOUNT_RATE = (TIER2_SCALE_FACTOR - MAX_DISCOUNT_FACTOR) /
            (MAX_DISCOUNT_THRESHOLD - TIER2_THRESHOLD);

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int buttonPressed = event.whichButton() + 1;

        int playerLevel = player.getLevel();
        int expCost = calculateScaledExpCost(playerLevel, buttonPressed);

        if (player.getLevel() > 30) {
            int totalExp = getTotalExperience(player, buttonPressed);
            setTotalExperience(player, totalExp - expCost);
        }
    }

    /**
     * Calculates the scaled experience cost based on player level
     * @param level The player's current level
     * @param button The enchantment button pressed (1-3)
     * @return The scaled cost in experience points
     */
    private int calculateScaledExpCost(int level, int button) {
        // Calculate the vanilla cost (3 levels worth of XP at the player's current level)
        int vanillaCost = getXpForLevel(level) - getXpForLevel(level - 3);

        // Apply discount based on player level
        double scaleFactor = calculateScaleFactor(level);

        // Return the scaled version of the vanilla cost
        return (int)(vanillaCost * scaleFactor);
    }

    /**
     * Calculates the discount scale factor based on player level
     * @param level The player's current level
     * @return A scale factor between 0.4 and 1.0
     */
    private double calculateScaleFactor(int level) {
        if (level <= NO_DISCOUNT_THRESHOLD) {
            // No discount for low levels
            return BASE_SCALE_FACTOR;
        } else if (level <= TIER1_THRESHOLD) {
            // First tier: reduce from 100% to 80% between levels 30-250
            return BASE_SCALE_FACTOR - ((level - NO_DISCOUNT_THRESHOLD) * TIER1_DISCOUNT_RATE);
        } else if (level <= TIER2_THRESHOLD) {
            // Second tier: reduce from 80% to 60% between levels 250-500
            return TIER1_SCALE_FACTOR - ((level - TIER1_THRESHOLD) * TIER2_DISCOUNT_RATE);
        } else if (level <= MAX_DISCOUNT_THRESHOLD) {
            // Third tier: reduce from 60% to 40% between levels 500-1000
            return TIER2_SCALE_FACTOR - ((level - TIER2_THRESHOLD) * TIER3_DISCOUNT_RATE);
        } else {
            // Maximum discount of 60% (40% of original cost) at level 1000 and above
            return MAX_DISCOUNT_FACTOR;
        }
    }

    /**
     * Calculates total XP required for a specific level
     * @param level The level to calculate XP for
     * @return Total XP required to reach that level
     */
    private int getXpForLevel(int level) {
        if (level <= 0) return 0;

        if (level <= 16) {
            return (int)(Math.pow(level, 2) + 6 * level);
        } else if (level <= 31) {
            return (int)(2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            return (int)(4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
    }

    /**
     * Returns the XP required for a single level
     * @param level The level to get XP for
     * @return XP required for just that level
     */
    private int getExpAtLevel(int level) {
        return level <= 15 ? (2 * level) + 7
                : level <= 30 ? (5 * level) - 38
                : (9 * level) - 158;
    }

    /**
     * Gets the total experience points a player has
     * @param player The player to check
     * @param button The enchantment button pressed
     * @return Total experience points
     */
    private int getTotalExperience(Player player, int button) {
        int currentLevel = player.getLevel();
        int totalExp = Math.round(getExpAtLevel(currentLevel) * player.getExp());
        for (int lvl = 0; lvl < currentLevel + button; lvl++) {
            totalExp += getExpAtLevel(lvl);
        }
        return totalExp < 0 ? Integer.MAX_VALUE : totalExp;
    }

    /**
     * Sets a player's total experience to a specific value
     * @param player The player to modify
     * @param exp The experience points to set
     */
    protected void setTotalExperience(Player player, int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int remainingExp = exp;
        while (remainingExp > 0) {
            int expForNextLevel = getExpAtLevel(player.getLevel());
            if (remainingExp >= expForNextLevel) {
                player.giveExp(expForNextLevel);
                remainingExp -= expForNextLevel;
            } else {
                player.giveExp(remainingExp);
                remainingExp = 0;
            }
        }
    }
}
