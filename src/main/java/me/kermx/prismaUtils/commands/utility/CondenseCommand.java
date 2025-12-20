package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.feature.CondenseManager;
import me.kermx.prismaUtils.managers.feature.CondenseMaterialsManager;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class CondenseCommand extends BaseCommand {

    private final CondenseMaterialsManager cmm;
    private final CondenseManager condenseManager;

    public CondenseCommand() {
        super("prismautils.command.condense", false, "/condense [all|reversible|hand|material]");
        this.cmm = new CondenseMaterialsManager();
        this.condenseManager = new CondenseManager(cmm);
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Map<Material, Integer> recipes;

        if (args.length == 0 || args[0].equalsIgnoreCase("reversible")) {
            List<CondenseManager.Conversion> conversions = condenseManager.condense(player, CondenseManager.Mode.REVERSIBLE_ONLY, null);
            if (conversions.isEmpty()) {
                player.sendMessage(TextUtils.deserializeString("You don't have any items to condense."));
            } else {
                for (CondenseManager.Conversion c : conversions) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                    Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                                    Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
                    );
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            List<CondenseManager.Conversion> conversions = condenseManager.condense(player, CondenseManager.Mode.ALL, null);
            if (conversions.isEmpty()) {
                player.sendMessage(TextUtils.deserializeString("You don't have any items to condense."));
            } else {
                for (CondenseManager.Conversion c : conversions) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                    Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                                    Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
                    );
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("hand")) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Material handMaterial = itemInHand.getType();

            if (handMaterial == Material.AIR) {
                player.sendMessage(TextUtils.deserializeString("You are not holding any item."));
                return true;
            }

            List<CondenseManager.Conversion> conversions = condenseManager.condense(player, CondenseManager.Mode.ALL, handMaterial);
            if (conversions.isEmpty()) {
                player.sendMessage(TextUtils.deserializeString("You don't have enough items to condense."));
            } else {
                CondenseManager.Conversion c = conversions.getFirst();
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
                );
            }
            return true;
        }

        if (args.length == 1) {
            Material material = Material.matchMaterial(args[0].toUpperCase());
            if (material == null || !cmm.getRecipes().containsKey(material)) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().incorrectUsageMessage)
                );
                return true;
            }

            List<CondenseManager.Conversion> conversions = condenseManager.condense(player, CondenseManager.Mode.ALL, material);
            if (conversions.isEmpty()) {
                player.sendMessage(TextUtils.deserializeString("You don't have enough items to condense."));
            } else {
                CondenseManager.Conversion c = conversions.getFirst();
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
                );
            }
            return true;
        }

        return false;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return super.onTabCompleteExecute(sender, args);
        }

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            if ("all".startsWith(partialArg)) suggestions.add("all");
            if ("reversible".startsWith(partialArg)) suggestions.add("reversible");
            if ("hand".startsWith(partialArg)) suggestions.add("hand");

            Map<Material, Integer> recipes = cmm.getRecipes();

            Set<Material> materialsInInventory = new HashSet<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    Material mat = item.getType();
                    if (recipes.containsKey(mat)) {
                        materialsInInventory.add(mat);
                    }
                }
            }

            for (Material material : materialsInInventory) {
                String materialName = material.name().toLowerCase();
                if (materialName.startsWith(partialArg)) {
                    suggestions.add(materialName);
                }
            }

            return suggestions;
        }
        return super.onTabCompleteExecute(sender, args);
    }
}