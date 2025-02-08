package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class EntityInfoCommand extends BaseCommand {

    public EntityInfoCommand(){
        super("prismautils.command.entityinfo", false, "/entityinfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
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

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
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
