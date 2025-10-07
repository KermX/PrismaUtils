package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadConfigCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public ReloadConfigCommand(PrismaUtils plugin) {
        super("prismautils.command.reload", true, "/prismautilsreload [config]");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            return reloadAllConfigs(sender);
        }
        if (args[0].equalsIgnoreCase("all")) {
            return reloadAllConfigs(sender);
        }

        if (args[0].equalsIgnoreCase("list")) {
            return listConfigs(sender);
        }

        return reloadSpecificConfig(sender, args[0]);
    }

    private boolean reloadAllConfigs(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        sender.sendMessage(TextUtils.deserializeString("<yellow>Reloading all PrismaUtils configurations..."));

        try {
            ConfigManager.getInstance().reloadAll();

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(TextUtils.deserializeString(
                    "<green>All configurations reloaded successfully in " + duration + "ms!"
            ));
            return true;

        } catch (Exception e) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Error reloading configurations: " + e.getMessage()
            ));
            plugin.getLogger().severe("Error reloading configurations:");
            e.printStackTrace();
            return false;
        }
    }

    private boolean reloadSpecificConfig(CommandSender sender, String configName) {
        ConfigManager.ConfigType configType = ConfigManager.ConfigType.fromName(configName);

        if (configType == null) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Unknown config: <white>" + configName +
                            "<red>. Use <white>/prismautilsreload list <red>to see available configs."
            ));
            return false;
        }

        long startTime = System.currentTimeMillis();
        sender.sendMessage(TextUtils.deserializeString(
                "<yellow>Reloading <white>" + configType.getName() + ".yml<yellow>..."
        ));

        try {
            boolean success = ConfigManager.getInstance().reload(configType);

            if (success) {
                long duration = System.currentTimeMillis() - startTime;
                sender.sendMessage(TextUtils.deserializeString(
                        "<green>Config <white>" + configType.getName() +
                                ".yml <green>reloaded successfully in " + duration + "ms!"
                ));
            } else {
                sender.sendMessage(TextUtils.deserializeString(
                        "<red>Failed to reload config: <white>" + configType.getName() + ".yml"
                ));
            }

            return success;

        } catch (Exception e) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Error reloading config: " + e.getMessage()
            ));
            plugin.getLogger().severe("Error reloading " + configType.getName() + " config:");
            e.printStackTrace();
            return false;
        }
    }

    private boolean listConfigs(CommandSender sender) {
        sender.sendMessage(TextUtils.deserializeString("<gold><b>Available Configuration Files:</b>"));
        sender.sendMessage(TextUtils.deserializeString("<gray>Use <white>/prismautilsreload <config> <gray>to reload a specific file"));
        sender.sendMessage(TextUtils.deserializeString("<gray>Use <white>/prismautilsreload all <gray>to reload all files"));
        sender.sendMessage("");

        for (ConfigManager.ConfigType type : ConfigManager.ConfigType.values()) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<yellow>• <white>" + type.getName() + ".yml <gray>- " + type.getDescription()
            ));
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("all");
            completions.add("list");

            // Add all config names
            completions.addAll(ConfigManager.ConfigType.getNames());

            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}