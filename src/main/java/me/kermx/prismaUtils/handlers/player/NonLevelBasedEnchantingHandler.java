package me.kermx.prismaUtils.handlers.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class NonLevelBasedEnchantingHandler implements Listener {

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int buttonPressed = event.whichButton() + 1;
        int expCost = 133 * buttonPressed;
        if (player.getLevel() > 30) {
            int totalExp = getTotalExperience(player, buttonPressed);
            setTotalExperience(player, totalExp - expCost);
        }
    }

    private int getExpAtLevel(int level) {
        return level <= 15 ? (2 * level) + 7
                : level <= 30 ? (5 * level) - 38
                : (9 * level) - 158;
    }

    private int getTotalExperience(Player player, int button) {
        int currentLevel = player.getLevel();
        int totalExp = Math.round(getExpAtLevel(currentLevel) * player.getExp());
        for (int lvl = 0; lvl < currentLevel + button; lvl++) {
            totalExp += getExpAtLevel(lvl);
        }
        return totalExp < 0 ? Integer.MAX_VALUE : totalExp;
    }

    public void setTotalExperience(Player player, int exp) {
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
