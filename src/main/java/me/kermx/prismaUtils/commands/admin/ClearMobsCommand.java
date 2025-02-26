package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import java.util.*;

public class ClearMobsCommand extends BaseCommand {

    public ClearMobsCommand() {
        super("prismautils.command.clearmobs", false, "/clearmobs [flags]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Map<String, String> flags = parseFlags(args);

        double radius = 10.0;
        if (flags.containsKey("radius")) {
            try {
                radius = Double.parseDouble(flags.get("radius"));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid radius value: " + flags.get("radius"), NamedTextColor.RED));
                return false;
            }
        }

        EntityType targetType = null;
        if (flags.containsKey("mobtype")) {
            try {
                targetType = EntityType.valueOf(flags.get("mobtype").toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Invalid mob type: " + flags.get("mobtype"), NamedTextColor.RED));
                return false;
            }
        }

        int limit = 0; // 0 means no limit.
        if (flags.containsKey("limit")) {
            try {
                limit = Integer.parseInt(flags.get("limit"));
                if (limit < 0) {
                    player.sendMessage(Component.text("Limit cannot be negative.", NamedTextColor.RED));
                    return false;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid limit value: " + flags.get("limit"), NamedTextColor.RED));
                return false;
            }
        }

        boolean includeTamed = flags.containsKey("includetamed") && Boolean.parseBoolean(flags.get("includetamed"));
        boolean excludeNamed = flags.containsKey("excludenamed") && Boolean.parseBoolean(flags.get("excludenamed"));
        boolean preview = flags.containsKey("preview") && Boolean.parseBoolean(flags.get("preview"));

        int count = 0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                continue;
            }

            if (targetType != null) {
                if (entity.getType() != targetType) {
                    continue;
                }
            } else {
                if (!(entity instanceof LivingEntity || entity.getType() == EntityType.ARMOR_STAND)) {
                    continue;
                }
            }

            if (excludeNamed && entity.customName() != null) {
                continue;
            }

            if (!includeTamed && entity instanceof Tameable) {
                if (((Tameable) entity).isTamed()) {
                    continue;
                }
            }

            if (!preview) {
                entity.remove();
            }
            count++;

            if (limit > 0 && count >= limit) {
                break;
            }
        }

        if (preview) {
            player.sendMessage(Component.text("Preview: " + count + " mob" + (count == 1 ? "" : "s") +
                    " would be killed within " + radius + " blocks.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Killed " + count + " mob" + (count == 1 ? "" : "s") +
                    " within " + radius + " blocks.", NamedTextColor.GREEN));
        }
        return true;
    }

    private Map<String, String> parseFlags(String[] args) {
        Map<String, String> flags = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String flagName = arg.substring(2).toLowerCase();
                String value = "true"; // Default value for flags without an explicit value.
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    value = args[i + 1];
                    i++; // Skip the next argument since it's been consumed as the value.
                }
                flags.put(flagName, value);
            }
        }
        return flags;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        // Available flags.
        String[] availableFlags = {"radius", "mobtype", "limit", "includetamed", "excludenamed", "preview"};

        // If no arguments, suggest all flags.
        if (args.length == 0) {
            for (String flag : availableFlags) {
                completions.add("--" + flag);
            }
            return completions;
        }

        String current = args[args.length - 1];

        Set<String> usedFlags = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String flagName = args[i].substring(2).toLowerCase();
                usedFlags.add(flagName);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    i++;
                }
            }
        }

        // If current argument starts with "--", suggest flag names.
        if (current.startsWith("--")) {
            String prefix = current.substring(2).toLowerCase();
            for (String flag : availableFlags) {
                if (!usedFlags.contains(flag) && flag.startsWith(prefix)) {
                    completions.add("--" + flag);
                }
            }
            return completions;
        }

        // Otherwise, check if previous argument is a flag expecting a value.
        if (args.length >= 2 && args[args.length - 2].startsWith("--")) {
            String flagName = args[args.length - 2].substring(2).toLowerCase();
            // For boolean flags, suggest true/false.
            if (flagName.equals("includetamed") || flagName.equals("excludenamed") || flagName.equals("preview")) {
                String[] boolOptions = {"true", "false"};
                for (String option : boolOptions) {
                    if (option.startsWith(current.toLowerCase())) {
                        completions.add(option);
                    }
                }
                return completions;
            }
            // For mobtype, suggest valid EntityType names (for LivingEntities and ArmorStands, excluding PLAYER).
            if (flagName.equals("mobtype")) {
                for (EntityType type : EntityType.values()) {
                    if ((type.getEntityClass() != null && LivingEntity.class.isAssignableFrom(type.getEntityClass()) && type != EntityType.PLAYER)
                            || type == EntityType.ARMOR_STAND) {
                        String typeName = type.name().toLowerCase();
                        if (typeName.startsWith(current.toLowerCase())) {
                            completions.add(type.name());
                        }
                    }
                }
                return completions;
            }
            // For numeric flags (radius and limit), no suggestions are provided.
        }

        for (String flag : availableFlags) {
            if (!usedFlags.contains(flag)) {
                completions.add("--" + flag);
            }
        }
        return completions;
    }
}
