package me.kermx.prismaUtils.Commands.player.restore;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.entity.Player;

public class RestoreHungerCommand extends BaseRestoreAction {

    public RestoreHungerCommand() {
        super("feed", "prismautils.command.feed", "prismautils.command.feed.others", "prismautils.command.feed.all",
                ConfigManager.getInstance().getMessagesConfig().feedMessage,
                ConfigManager.getInstance().getMessagesConfig().feedAllMessage,
                ConfigManager.getInstance().getMessagesConfig().feedOtherMessage,
                ConfigManager.getInstance().getMessagesConfig().feedFedByOtherMessage);
    }

    @Override
    protected void performAction(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
    }
}
