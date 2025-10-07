package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.core.BaseCommand;
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

    public CondenseCommand() {
        super("prismautils.command.condense", false, "/condense [all|reversible|hand|material]");
        this.cmm = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Map<Material, Integer> recipes;

        // Determine which recipes to use based on arguments
        if (args.length == 0 || args[0].equalsIgnoreCase("reversible")) {
            recipes = cmm.getReversibleRecipes();
        } else if (args[0].equalsIgnoreCase("all")) {
            recipes = cmm.getRecipes();
        } else if (args[0].equalsIgnoreCase("hand")) {
            // Handle 'hand' argument
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Material handMaterial = itemInHand.getType();

            if (handMaterial == Material.AIR) {
                player.sendMessage(TextUtils.deserializeString("You are not holding any item."));
                return true;
            }

            if (!cmm.getRecipes().containsKey(handMaterial)) {
                player.sendMessage(TextUtils.deserializeString("The item in your hand cannot be condensed."));
                return true;
            }

            int condensedItems = condenseSpecificItem(player, handMaterial);
            if (condensedItems > 0) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(handMaterial))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(cmm.getResultMaterial(handMaterial, false)))))
                );
            } else {
                player.sendMessage(TextUtils.deserializeString("You don't have enough items to condense."));
            }
            return true;
        } else {
            recipes = cmm.getRecipes();
        }

        // Handle specific material argument
        if (args.length == 1 && !args[0].equalsIgnoreCase("all") && !args[0].equalsIgnoreCase("reversible")) {
            Material material = Material.matchMaterial(args[0].toUpperCase());
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
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(material))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(cmm.getResultMaterial(material, false)))))
                );
            } else {
                player.sendMessage(TextUtils.deserializeString("You don't have enough items to condense."));
            }
        } else {
            // Condense all eligible items
            boolean condensedAny = false;
            for (Material material : recipes.keySet()) {
                int condensedItems = condenseSpecificItem(player, material);
                if (condensedItems > 0) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                    Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(material))),
                                    Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(cmm.getResultMaterial(material, false)))))
                    );
                    condensedAny = true;
                }
            }
            if (!condensedAny) {
                player.sendMessage(TextUtils.deserializeString("You don't have any items to condense."));
            }
        }
        return true;
    }

    private int condenseSpecificItem(Player player, Material material) {
        if (!cmm.getRecipes().containsKey(material)) return 0;

        PlayerInventory playerInventory = player.getInventory();
        int inputAmount = cmm.getRecipes().get(material);
        int count = ItemUtils.countItems(playerInventory.getContents(), material);
        if (count < inputAmount) return 0;

        int condensedBlocks = count / inputAmount;
        int remainingItems = count % inputAmount;

        // Remove the items from the player's inventory
        playerInventory.removeItem(new ItemStack(material, count));
        // Add the condensed items and remaining items back to the inventory
        ItemUtils.giveItems(player, cmm.getResultMaterial(material, false), condensedBlocks);
        if (remainingItems > 0) {
            ItemUtils.giveItems(player, material, remainingItems);
        }

        // Handle any items to give back (e.g., empty bottles)
        if (cmm.getGiveBackEmptyMappings().containsKey(material)) {
            Material emptyMaterial = cmm.getGiveBackEmptyMappings().get(material);
            ItemUtils.giveItems(player, emptyMaterial, count - remainingItems);
        }

        return count; // Return the number of condensed items
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return super.onTabCompleteExecute(sender, args);
        }

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            // Add "all", "reversible", and "hand" if they match the partial input
            if ("all".startsWith(partialArg)) {
                suggestions.add("all");
            }
            if ("reversible".startsWith(partialArg)) {
                suggestions.add("reversible");
            }
            if ("hand".startsWith(partialArg)) {
                suggestions.add("hand");
            }

            // Get eligible materials
            Map<Material, Integer> recipes = cmm.getRecipes();

            // Get materials present in player's inventory that are eligible
            Set<Material> materialsInInventory = new HashSet<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    Material mat = item.getType();
                    if (recipes.containsKey(mat)) {
                        materialsInInventory.add(mat);
                    }
                }
            }

            // Filter materials based on partial input and add to suggestions
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