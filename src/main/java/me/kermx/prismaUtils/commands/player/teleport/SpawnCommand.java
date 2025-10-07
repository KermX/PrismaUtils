package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand extends BaseCommand {
    private final PrismaUtils plugin;

    public SpawnCommand(PrismaUtils plugin) {
        super("prismautils.command.spawn", false, "/spawn");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (!ConfigManager.getInstance().getMainConfig().spawnEnabled) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Spawn is not enabled on this server."
            ));
            return true;
        }

        String worldName = ConfigManager.getInstance().getMainConfig().spawnWorld;
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>The world <yellow>" + worldName + "<red> does not exist."
            ));
            return true;
        }

        double x = ConfigManager.getInstance().getMainConfig().spawnX;
        double y = ConfigManager.getInstance().getMainConfig().spawnY;
        double z = ConfigManager.getInstance().getMainConfig().spawnZ;
        float yaw = ConfigManager.getInstance().getMainConfig().spawnYaw;
        float pitch = ConfigManager.getInstance().getMainConfig().spawnPitch;

        Location spawnLocation = new Location(world, x, y, z, yaw, pitch);

        // Save the current location for /back command
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.setLastLocation(player.getLocation().clone());
        }

        // Teleport player to spawn
        player.teleportAsync(spawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported to spawn."
        ));

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion for this command
        return new ArrayList<>();
    }
}
