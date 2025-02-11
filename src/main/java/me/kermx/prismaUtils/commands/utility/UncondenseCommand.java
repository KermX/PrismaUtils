package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.features.CondenseMaterialsManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.PlayerUtils;
import org.bukkit.Bukkit;
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
        Player player = (Player) sender;
        if (args.length == 0) {
            uncondenseInventory(player);
            player.sendMessage("Uncondense all reversible recipes items inventory.");
            return true;
        }

        // TODO handle argument for specific item uncondense
        Material inputMaterial = Material.matchMaterial(args[0]);
        if (inputMaterial == null || !condenseMaterialsManager.getRecipes().containsKey(inputMaterial)) {
            player.sendMessage("Invalid item or no condensing recipe available for this item.");
            return true;
        }

        // Check for special NBT tags on the input item
        ItemStack inputItem = new ItemStack(inputMaterial);
        if (!ItemUtils.itemHasSpecialMeta(inputItem)) {
            // amount (i.e 9) to get condensed
            int inputAmount = condenseMaterialsManager.getRecipes().get(inputMaterial);
            // amount in player's inventory total
            int inputCount = ItemUtils.countItems(player.getInventory().getStorageContents(), inputMaterial);

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
        return true;
    }

    private void uncondenseInventory(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] contents = PlayerUtils.getMainInventory(player);

        Map<Material, Material> reversibleMaterialMappings = condenseMaterialsManager.getReversibleMaterialMappings(true);
        Map<Material, Integer> reversibleRecipes = condenseMaterialsManager.getReversibleRecipes();

        for (Material material : reversibleMaterialMappings.keySet()) {
            Material resultMaterial = reversibleMaterialMappings.get(material);

            // Check if the resultMaterial exists in reversibleRecipes
            if (resultMaterial != null && reversibleRecipes.containsKey(resultMaterial)) {
                int outputAmount = reversibleRecipes.get(resultMaterial);
                int count = 0;

                for (ItemStack item : contents) {
                    if (item != null && item.getType() == material && !ItemUtils.itemHasSpecialMeta(item)) {
                        count += item.getAmount();
                    }
                }

                if (count >= 1) {
                    int totalUncondensedItems = count * outputAmount;
                    int stacks = (int) Math.round((double) totalUncondensedItems / 64 - 0.5);
                    int leftovers = totalUncondensedItems % 64;
                    ArrayList<ItemStack> stacksToAddList = new ArrayList<>();

                    ItemStack inputStack = new ItemStack(material, count);
                    playerInventory.removeItem(inputStack);

                    for (int i = 0; i < stacks; i++) {
                        stacksToAddList.add(new ItemStack(resultMaterial, 64));
                    }
                    if (leftovers > 0) {
                        stacksToAddList.add(new ItemStack(resultMaterial, leftovers));
                    }

                    for (int i = 0; i < stacksToAddList.size(); i++) {
                        ItemUtils.giveItems(player, stacksToAddList.get(i));
                    }

                }
            } else {
                // Log or handle the case where the resultMaterial is not found in reversibleRecipes
                Bukkit.getLogger().warning("Material " + material + " does not have a corresponding recipe.");
            }
        }
    }

    private int emptyInventorySpaces(ItemStack[] contents) {
        int emptySpaces = 0;
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                emptySpaces++;
            }
        }
        return emptySpaces;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // TODO Fix tab completer
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
