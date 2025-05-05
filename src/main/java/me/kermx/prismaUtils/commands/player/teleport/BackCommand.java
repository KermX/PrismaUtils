package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class BackCommand extends BaseCommand {
    private final PlayerDataManager playerDataManager;

    public BackCommand(PlayerDataManager playerDataManager) {
        super("prismautils.command.back", false, "/back");
        this.playerDataManager = playerDataManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        Location lastLocation = playerData.getLastLocation();

        if (lastLocation == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have a previous location to return to."
            ));
            return true;
        }

        if (!ConfigManager.getInstance().getMainConfig().backWhitelistedWorlds.contains(lastLocation.getWorld().getName())) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You can't return to that world."
            ));
            return true;
        }

        Location currentLocation = player.getLocation().clone();

        // Teleport the player
        player.teleportAsync(lastLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported to your previous location."
        ));

        // Set the current location as the new last location
        playerData.setLastLocation(currentLocation);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion needed for this command
        return super.onTabCompleteExecute(sender, args);
    }
}
