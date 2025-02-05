package me.kermx.prismaUtils.Commands.AdminCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntityInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("prismautils.command.entityinfo")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        Entity targetEntity = getTargetEntity(player);

        if (targetEntity != null) {
            player.sendMessage("Entity Info:");
            player.sendMessage("Type: " + targetEntity.getType().name());
            player.sendMessage("Custom Name: " + (targetEntity.getCustomName() != null ? targetEntity.getCustomName() : "None"));
            player.sendMessage("Health: " + (targetEntity instanceof org.bukkit.entity.Damageable ? ((org.bukkit.entity.Damageable) targetEntity).getHealth() : "N/A"));
            player.sendMessage("Location: " + targetEntity.getLocation());
            player.sendMessage("UUID: " + targetEntity.getUniqueId());
        } else {
            player.sendMessage("You must be looking at an entity to use this command!");
        }

        return true;
    }

    private Entity getTargetEntity(Player player) {
        double range = 10.0;
        Vector direction = player.getEyeLocation().getDirection().normalize();
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            Vector toEntity = entity.getLocation().toVector().subtract(player.getEyeLocation().toVector());
            if (toEntity.normalize().dot(direction) > 0.98) {
                return entity;
            }
        }
        return null;
    }
}
