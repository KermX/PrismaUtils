package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class EntityInfoCommand extends BaseCommand {

    public EntityInfoCommand() {
        super("prismautils.command.entityinfo", false, "/entityinfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        Entity targetEntity = getTargetEntity(player);

        if (targetEntity != null) {
            player.sendMessage(Component.text("Entity Info:", NamedTextColor.GREEN));

            // Type
            player.sendMessage(Component.text("Type: ", NamedTextColor.YELLOW));
            String type = targetEntity.getType().name();
            TextUtils.sendCopyableMessage(player, type, type);

            // Custom Name
            player.sendMessage(Component.text("Custom Name: ", NamedTextColor.YELLOW));
            String customName = (targetEntity.getCustomName() != null) ? targetEntity.getCustomName() : "None";
            TextUtils.sendCopyableMessage(player, customName, customName);

            // Health
            player.sendMessage(Component.text("Health: ", NamedTextColor.YELLOW));
            String health = "N/A";
            if (targetEntity instanceof Damageable) {
                health = String.valueOf(((Damageable) targetEntity).getHealth());
            }
            TextUtils.sendCopyableMessage(player, health, health);

            // Location
            player.sendMessage(Component.text("Location: ", NamedTextColor.YELLOW));
            String location = targetEntity.getLocation().toString();
            TextUtils.sendCopyableMessage(player, location, location);

            // UUID
            player.sendMessage(Component.text("UUID: ", NamedTextColor.YELLOW));
            String uuid = targetEntity.getUniqueId().toString();
            TextUtils.sendCopyableMessage(player, uuid, uuid);
        } else {
            player.sendMessage(Component.text("You must be looking at an entity to use this command!", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
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