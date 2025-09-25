package me.kermx.prismaUtils.commands.player.restore;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class RestoreHealthCommand extends BaseRestoreAction {

    public RestoreHealthCommand() {
        super("heal", "prismautils.command.heal", "prismautils.command.heal.others", "prismautils.command.heal.all",
                ConfigManager.getInstance().getMessagesConfig().healMessage,
                ConfigManager.getInstance().getMessagesConfig().healAllMessage,
                ConfigManager.getInstance().getMessagesConfig().healOtherMessage,
                ConfigManager.getInstance().getMessagesConfig().healHealedByOtherMessage);
    }

    @Override
    protected void performAction(Player player) {
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20.0F); // Max saturation
    }
}