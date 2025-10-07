package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpawnMobCommand extends BaseCommand {

    private static final int MAX_SPAWN_AMOUNT = 100;

    public SpawnMobCommand() {
        super("prismautils.command.spawnmob", false, "/spawnmob <mob> [amount]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        EntityType mobType;
        try {
            mobType = EntityType.valueOf(args[0].toUpperCase());
            if (!mobType.isSpawnable() || !mobType.isAlive()) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1 || amount > MAX_SPAWN_AMOUNT) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        Location spawnLocation = ((Player) sender).getLocation();
        for (int i = 0; i < amount; i++) {
            spawnLocation.getWorld().spawnEntity(spawnLocation, mobType);
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (EntityType type : EntityType.values()) {
                if (type.isSpawnable() && type.isAlive()) {
                    completions.add(type.name().toLowerCase());
                }
            }
        }
        return completions;
    }
}