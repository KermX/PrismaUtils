package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.core.FeatureToggleManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeaturesCommand extends BaseCommand {
    private final PrismaUtils plugin;

    public FeaturesCommand(PrismaUtils plugin) {
        super("prismautils.command.features", true, "/features");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        FeatureToggleManager ftm = plugin.getFeatureToggleManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(TextUtils.deserializeString("<gold>========== PrismaUtils Features =========="));

            Map<FeatureToggleManager.Feature, Boolean> features = ftm.getAllFeatures();
            features.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey((a, b) -> a.getConfigKey().compareTo(b.getConfigKey())))
                    .forEach(entry -> {
                        String status = entry.getValue() ? "<green>✓ ENABLED" : "<red>✗ DISABLED";
                        sender.sendMessage(TextUtils.deserializeString(
                                status + " <gray>- <white>" + entry.getKey().getConfigKey() +
                                        " <dark_gray>(" + entry.getKey().getDescription() + ")"
                        ));
                    });

            sender.sendMessage(TextUtils.deserializeString("<gold>=========================================="));
            sender.sendMessage(TextUtils.deserializeString("<yellow>Use /features toggle <feature> to toggle a feature"));
            return true;
        }
        // /features toggle <feature>
        if (args[0].equalsIgnoreCase("toggle") && args.length >= 2) {
            String featureName = String.join(".", Arrays.copyOfRange(args, 1, args.length));

            FeatureToggleManager.Feature feature = null;
            for (FeatureToggleManager.Feature f : FeatureToggleManager.Feature.values()) {
                if (f.getConfigKey().equalsIgnoreCase(featureName)) {
                    feature = f;
                    break;
                }
            }

            if (feature == null) {
                sender.sendMessage(TextUtils.deserializeString("<red>Unknown feature: " + featureName));
                sender.sendMessage(TextUtils.deserializeString("<yellow>Use /features list to see all features"));
                return true;
            }

            boolean newState = !ftm.isEnabled(feature);
            ftm.setEnabled(feature, newState);

            String status = newState ? "<green>ENABLED" : "<red>DISABLED";
            sender.sendMessage(TextUtils.deserializeString(
                    status + " <gray>feature: <white>" + feature.getConfigKey()
            ));
            sender.sendMessage(TextUtils.deserializeString(
                    "<yellow>⚠ You must reload the plugin for this change to take effect: <white>/prismautilsreload"
            ));
            return true;
        }

        // /features reload
        if (args[0].equalsIgnoreCase("reload")) {
            ftm.reload();
            sender.sendMessage(TextUtils.deserializeString("<green>Reloaded features.yml configuration"));
            return true;
        }

        sender.sendMessage(TextUtils.deserializeString("<red>Usage: /features [list|toggle <feature>|reload]"));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "toggle", "reload").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("toggle")) {
            String partial = String.join(".", Arrays.copyOfRange(args, 1, args.length));
            return Arrays.stream(FeatureToggleManager.Feature.values())
                    .map(FeatureToggleManager.Feature::getConfigKey)
                    .filter(key -> key.toLowerCase().startsWith(partial.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return super.onTabCompleteExecute(sender, args);
    }
}
