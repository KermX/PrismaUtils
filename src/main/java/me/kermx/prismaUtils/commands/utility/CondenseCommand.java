package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.features.CondenseMaterialsManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class CondenseCommand extends BaseCommand{

    private final CondenseMaterialsManager condenseMaterialsManager;

    public CondenseCommand() {
        super("prismautils.command.condense", false, "/condense [all|hand|item]");
        this.condenseMaterialsManager = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {

        Player player = (Player) sender;
        if (args.length == 0) {
            condenseReversibleItems(player);
            player.sendMessage("Condensed all reversible recipes items inventory.");
            return true;
        }
        if (args.length == 1) {
            String argument = args[0].toLowerCase();

            if ((argument.equals("inventory") || argument.equals("all") || argument.equals("inv"))) {
                condenseInventory(player);
                player.sendMessage("Condensed all eligible items in your inventory.");
                return true;
            } else {
                Material inputMaterial = Material.matchMaterial(args[0]);
                if (inputMaterial == null || !condenseMaterialsManager.getRecipes().containsKey(inputMaterial)) {
                    player.sendMessage("Invalid item or no condensing recipe available for this item.");
                    return true;
                }

                // Check for special NBT tags on the input item
                ItemStack inputItem = new ItemStack(inputMaterial);
                if (!ItemUtils.itemHasSpecialMeta(inputItem)) {
                    int inputAmount = condenseMaterialsManager.getRecipes().get(inputMaterial);
                    int inputCount = ItemUtils.countItems(player.getInventory().getContents(), inputMaterial);

                    if (inputCount >= inputAmount) {
                        int condensedBlocks = inputCount / inputAmount;
                        int remainingItems = inputCount % inputAmount;

                        ItemStack inputStack = new ItemStack(inputMaterial, inputCount);
                        player.getInventory().removeItem(inputStack);

                        Material resultMaterial = condenseMaterialsManager.getResultMaterial(inputMaterial, false);
                        ItemUtils.giveItems(player, resultMaterial, condensedBlocks);
                        ItemUtils.giveItems(player, inputMaterial, remainingItems);
                        // player.sendMessage("Condensed " + condensedBlocks + " " + resultMaterial + " blocks.");
                    } else {
                        player.sendMessage("You don't have enough to condense.");
                    }
                } else {
                    player.sendMessage("Item cannot be condensed.");
                }
            }
        }
        return true;
    }

    private void condenseInventory(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] contents = PlayerUtils.getMainInventory(player);


        for (Material material : condenseMaterialsManager.getRecipes().keySet()) {
            int inputAmount = condenseMaterialsManager.getRecipes().get(material);
            int count = 0;

            for (ItemStack item : contents) {
                if (item != null && item.getType() == material && !ItemUtils.itemHasSpecialMeta(item)) {
                    count += item.getAmount();
                }
            }

            if (count >= inputAmount) {
                int condensedBlocks = count / inputAmount;
                int remainingItems = count % inputAmount;

                ItemStack inputStack = new ItemStack(material, count);
                playerInventory.removeItem(inputStack);

                Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);

                // Check if the condensed item has a corresponding empty bottle
                if (condenseMaterialsManager.getGiveBackEmptyMappings().containsKey(material)) {
                    Material emptyItemMaterial = condenseMaterialsManager.getGiveBackEmptyMappings().get(material);
                    int emptyItemAmount = count - remainingItems;
                    ItemUtils.giveItems(player, emptyItemMaterial, emptyItemAmount);
                }

                ItemUtils.giveItems(player, resultMaterial, condensedBlocks);
                ItemUtils.giveItems(player, material, remainingItems);
            }
        }
    }

    private void condenseReversibleItems(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] contents = PlayerUtils.getMainInventory(player);

        for (Material material : condenseMaterialsManager.getReversibleRecipes().keySet()) {
            int requiredAmount = condenseMaterialsManager.getReversibleRecipes().get(material);
            int amountInInventory = 0;

            for (ItemStack item : contents) {
                if (item != null && item.getType() == material && !ItemUtils.itemHasSpecialMeta(item)) {
                    amountInInventory += item.getAmount();
                }
            }

            if (amountInInventory >= requiredAmount) {
                int condensedBlocks = amountInInventory / requiredAmount;
                int remainingItems = amountInInventory % requiredAmount;

                ItemStack inputStack = new ItemStack(material, amountInInventory);
                playerInventory.removeItem(inputStack);

                Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);
                ItemUtils.giveItems(player, resultMaterial, condensedBlocks);
                ItemUtils.giveItems(player, material, remainingItems);
            }
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            for (Material material : condenseMaterialsManager.getRecipes().keySet()) {
                if (material.name().toLowerCase().startsWith(input)) {
                    suggestions.add("all");
                    suggestions.add("reversible");
                    suggestions.add(material.name());
                }
            }
            return suggestions;
        }
        return null; // Return null for default behavior
    }

}
