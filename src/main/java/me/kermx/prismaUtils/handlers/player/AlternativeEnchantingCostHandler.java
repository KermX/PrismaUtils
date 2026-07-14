package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class AlternativeEnchantingCostHandler implements Listener {

    private final PrismaUtils plugin;
    private final int buttonOneCost;
    private final int buttonTwoCost;
    private final int buttonThreeCost;

    public AlternativeEnchantingCostHandler(PrismaUtils plugin, int buttonOneCost, int buttonTwoCost, int buttonThreeCost) {
        this.plugin = plugin;
        this.buttonOneCost = buttonOneCost;
        this.buttonTwoCost = buttonTwoCost;
        this.buttonThreeCost = buttonThreeCost;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        int expCost = getCostForButton(event.whichButton());
        int currentExp = getTotalExperience(player);

        if (currentExp < expCost) {
            event.setCancelled(true);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().enchantingNotEnoughExpMessage,
                    Placeholder.component("exp", Component.text(expCost))
            ));
            return;
        }

        // Vanilla still deducts levels after this event returns, so overwrite the
        // player's experience on the next tick with exactly what we want them to keep.
        int remainingExp = currentExp - expCost;
        plugin.getServer().getScheduler().runTask(plugin, () -> setTotalExperience(player, remainingExp));
    }

    /**
     * Static experience point cost for the pressed enchantment button
     * @param whichButton The zero-indexed button from the event (0-2)
     * @return The cost in experience points
     */
    private int getCostForButton(int whichButton) {
        return switch (whichButton) {
            case 0 -> buttonOneCost;
            case 1 -> buttonTwoCost;
            default -> buttonThreeCost;
        };
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
     * @return Total experience points
     */
    private int getTotalExperience(Player player) {
        int currentLevel = player.getLevel();
        int totalExp = Math.round(getExpAtLevel(currentLevel) * player.getExp());
        for (int lvl = 0; lvl < currentLevel; lvl++) {
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
