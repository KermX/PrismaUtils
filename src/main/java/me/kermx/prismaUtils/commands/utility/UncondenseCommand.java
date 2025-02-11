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

import java.util.*;

public class UncondenseCommand extends BaseCommand {

    private final CondenseMaterialsManager condenseMaterialsManager;

    public UncondenseCommand() {
        super("prismautils.command.uncondense", false, "/uncondense [all|hand|item]");
        this.condenseMaterialsManager = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            uncondenseReversibleItems((Player) sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand){
            case "all":
            case "inventory":
            case "inv":
                uncondenseReversibleItems((Player) sender);
                // uncondenseCondensedAll
                break;
            case "hand":
                uncondenseHand((Player) sender);
                // uncondenseCondensedHand
                break;
            default:
                uncondenseSpecificItem((Player) sender, args[0]);
        }
        return true;
    }

    private void uncondenseReversibleItems(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] mainInventory = PlayerUtils.getMainInventory(player);

        Map<Material, Material> uncondenseMapping = condenseMaterialsManager.getReversibleMaterialMappings(true);
        Map<Material, Integer> reversibleRecipes = condenseMaterialsManager.getReversibleRecipes();

        uncondenseMapping.forEach((condensedMaterial, baseMaterial) -> {
            if (baseMaterial != null && reversibleRecipes.containsKey(baseMaterial)) {
                int outputAmount = reversibleRecipes.get(baseMaterial);
                int count = ItemUtils.countItems(mainInventory, condensedMaterial);
                if (count >= 1) {
                    // Total number of uncondensed items equals the count of condensed items multiplied
                    // by the recipe's output amount.
                    int totalUncondensedItems = count * outputAmount;
                    // Remove the condensed items from the inventory.
                    playerInventory.removeItem(new ItemStack(condensedMaterial, count));
                    // Distribute the uncondensed items into stacks (max 64 per stack).
                    List<ItemStack> stacksToAdd = new ArrayList<>();
                    while (totalUncondensedItems > 0) {
                        int stackSize = Math.min(64, totalUncondensedItems);
                        stacksToAdd.add(new ItemStack(baseMaterial, stackSize));
                        totalUncondensedItems -= stackSize;
                    }
                    // Give each stack to the player.
                    stacksToAdd.forEach(stack -> {
                        ItemUtils.giveItems(player, stack);
                    });
                }
            } else {
                // do stuff
            }
        });
    }

    /**
     * Uncondenses the item in the player's main hand, if it is a valid condensed item.
     */
    private void uncondenseHand(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack handItem = inventory.getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("Your hand is empty.");
            return;
        }
        if (ItemUtils.itemHasSpecialMeta(handItem)) {
            player.sendMessage("The item in your hand cannot be uncondensed due to custom meta.");
            return;
        }

        // Retrieve the uncondense mapping.
        Map<Material, Material> uncondenseMapping = condenseMaterialsManager.getReversibleMaterialMappings(true);
        Material condensedMaterial = handItem.getType();
        if (!uncondenseMapping.containsKey(condensedMaterial)) {
            player.sendMessage("The item in your hand cannot be uncondensed.");
            return;
        }
        Material baseMaterial = uncondenseMapping.get(condensedMaterial);
        Map<Material, Integer> reversibleRecipes = condenseMaterialsManager.getReversibleRecipes();
        if (!reversibleRecipes.containsKey(baseMaterial)) {
            player.sendMessage("No uncondense recipe exists for this item.");
            return;
        }
        int outputAmount = reversibleRecipes.get(baseMaterial);
        int count = handItem.getAmount();
        if (count < 1) {
            player.sendMessage("You don't have any condensed items in your hand to uncondense.");
            return;
        }

        // Remove the item from the hand.
        inventory.setItemInMainHand(null);
        int totalUncondensedItems = count * outputAmount;
        // Create stacks of up to 64 items each.
        while (totalUncondensedItems > 0) {
            int stackSize = Math.min(64, totalUncondensedItems);
            ItemUtils.giveItems(player, new ItemStack(baseMaterial, stackSize));
            totalUncondensedItems -= stackSize;
        }
        player.sendMessage("Uncondensed the item in your hand.");
    }

    /**
     * Uncondenses a specific material (provided as a string) in the player's inventory.
     *
     * @param player     the executing player
     * @param itemString the string representing the condensed material (e.g., "COAL_BLOCK")
     */
    private void uncondenseSpecificItem(Player player, String itemString) {
        if (!ItemUtils.isValidItemString(itemString)) {
            player.sendMessage("Invalid material: " + itemString);
            return;
        }
        Material condensedMaterial = Material.matchMaterial(itemString);
        if (condensedMaterial == null) {
            player.sendMessage("Invalid material: " + itemString);
            return;
        }
        Map<Material, Material> uncondenseMapping = condenseMaterialsManager.getReversibleMaterialMappings(true);
        if (!uncondenseMapping.containsKey(condensedMaterial)) {
            player.sendMessage("No uncondense recipe exists for " + condensedMaterial.name());
            return;
        }
        Material baseMaterial = uncondenseMapping.get(condensedMaterial);
        Map<Material, Integer> reversibleRecipes = condenseMaterialsManager.getReversibleRecipes();
        if (!reversibleRecipes.containsKey(baseMaterial)) {
            player.sendMessage("No reversible recipe found for " + baseMaterial.name());
            return;
        }
        int outputAmount = reversibleRecipes.get(baseMaterial);
        ItemStack[] contents = PlayerUtils.getMainInventory(player);
        int count = ItemUtils.countItems(contents, condensedMaterial);
        if (count < 1) {
            player.sendMessage("You don't have any " + condensedMaterial.name() + " to uncondense.");
            return;
        }
        // Remove the condensed items from the inventory.
        player.getInventory().removeItem(new ItemStack(condensedMaterial, count));
        int totalUncondensedItems = count * outputAmount;
        // Distribute the uncondensed items into stacks (max 64 per stack).
        while (totalUncondensedItems > 0) {
            int stackSize = Math.min(64, totalUncondensedItems);
            ItemUtils.giveItems(player, new ItemStack(baseMaterial, stackSize));
            totalUncondensedItems -= stackSize;
        }
        player.sendMessage("Uncondensed all " + condensedMaterial.name() + " in your inventory.");
    }
}
