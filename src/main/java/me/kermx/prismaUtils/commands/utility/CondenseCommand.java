package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.features.CondenseMaterialsManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CondenseCommand extends BaseCommand {

    private final CondenseMaterialsManager cmm;

    public CondenseCommand() {
        super("prismautils.command.condense", false, "/condense [all|hand|item]");
        this.cmm = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Map<Material, Integer> recipes = args.length == 0 || args[0].equalsIgnoreCase("reversible")
                ? cmm.getReversibleRecipes()
                : cmm.getRecipes();

        if (args.length == 1 && !args[0].equalsIgnoreCase("all") && !args[0].equalsIgnoreCase("inventory")) {
            Material material = Material.matchMaterial(args[0]);
            if (material == null || !recipes.containsKey(material)) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().incorrectUsageMessage)
                );
                return true;
            }
            int condensedItems = condenseSpecificItem(player, material);
            if (condensedItems > 0) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(cmm.normalizeMaterialName(material.name()))),
                                Placeholder.component("to", Component.text(cmm.normalizeMaterialName(cmm.getResultMaterial(material, false).name()))))
                );
            }
        } else {
            recipes.keySet().forEach(material -> {
                int condensedItems = condenseSpecificItem(player, material);
                if (condensedItems > 0) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                    Placeholder.component("from", Component.text(cmm.normalizeMaterialName(material.name()))),
                                    Placeholder.component("to", Component.text(cmm.normalizeMaterialName(cmm.getResultMaterial(material, false).name()))))
                    );
                }
            });
        }
        return true;
    }

    private int condenseSpecificItem(Player player, Material material) {
        if (!cmm.getRecipes().containsKey(material)) return 0;

        // Maybe make use of PlayerUtils.getMainInventory(player) here to exclude offhand & armor slots
        PlayerInventory playerInventory = player.getInventory();
        int inputAmount = cmm.getRecipes().get(material);
        int count = ItemUtils.countItems(playerInventory.getContents(), material);
        if (count < inputAmount) return 0;

        int condensedBlocks = count / inputAmount;
        int remainingItems = count % inputAmount;

        playerInventory.removeItem(new ItemStack(material, count));
        ItemUtils.giveItems(player, cmm.getResultMaterial(material, false), condensedBlocks);
        ItemUtils.giveItems(player, material, remainingItems);

        if (cmm.getGiveBackEmptyMappings().containsKey(material)) {
            Material emptyMaterial = cmm.getGiveBackEmptyMappings().get(material);
            ItemUtils.giveItems(player, emptyMaterial, count - remainingItems);
        }

        return count; // Return the number of condensed items
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(List.of("all", "reversible"));
            suggestions.addAll(cmm.getRecipes().keySet().stream()
                    .map(Enum::name)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList());
            return suggestions;
        }
        return null;
    }
}