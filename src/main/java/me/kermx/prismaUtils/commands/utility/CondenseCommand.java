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

public class CondenseCommand extends BaseCommand{

    private final CondenseMaterialsManager condenseMaterialsManager;

    public CondenseCommand() {
        super("prismautils.command.condense", false, "/condense [all|hand|item]");
        this.condenseMaterialsManager = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            condenseReversibleItems((Player) sender);
            // condenseCondensedAllReversible
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand){
            case "all":
            case "inventory":
            case "inv":
                condenseAll((Player) sender);
                // condenseCondensedAll
                break;
            case "hand":
                condenseHand((Player) sender);
                // condenseCondensedHand
                break;
            default:
                condenseSpecificItem((Player) sender, args[0]);
        }
        return true;
    }

    private void condenseReversibleItems(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] mainInventory = PlayerUtils.getMainInventory(player);

        condenseMaterialsManager.getReversibleRecipes().forEach(((material, requiredAmount) -> {
            int count = ItemUtils.countItems(mainInventory, material);
            if (count >= requiredAmount){
                int condensedBlocks = count / requiredAmount;
                int remainingItems = count % requiredAmount;

                playerInventory.removeItem(new ItemStack(material, count));
                Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);
                ItemUtils.giveItems(player, new ItemStack(resultMaterial, condensedBlocks));
                ItemUtils.giveItems(player, new ItemStack(material, remainingItems));
            }
        }));
    }

    private void condenseAll(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] mainInventory = PlayerUtils.getMainInventory(player);

        condenseMaterialsManager.getRecipes().forEach(((material, requiredAmount) -> {
            int count = ItemUtils.countItems(mainInventory, material);
            if (count >= requiredAmount){
                int condensedBlocks = count / requiredAmount;
                int remainingItems = count % requiredAmount;

                playerInventory.removeItem(new ItemStack(material, count));
                Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);

                if (condenseMaterialsManager.getGiveBackEmptyMappings().containsKey(material)){
                    Material emptyMaterial = condenseMaterialsManager.getGiveBackEmptyMappings().get(material);
                    int emptyAmount = count - remainingItems;
                    ItemUtils.giveItems(player, new ItemStack(emptyMaterial, emptyAmount));
                }

                ItemUtils.giveItems(player, new ItemStack(resultMaterial, condensedBlocks));
                ItemUtils.giveItems(player, new ItemStack(material, remainingItems));
            }
        }));
    }

    private void condenseHand(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack heldItem = playerInventory.getItemInMainHand();

        if (heldItem == null || heldItem.getType() == Material.AIR){
            // condenseHandEmpty
            return;
        }
        if (ItemUtils.itemHasSpecialMeta(heldItem)){
            // condenseCannotBeCondensedDueToMeta
            return;
        }

        Material material = heldItem.getType();
        if (!condenseMaterialsManager.getRecipes().containsKey(material)){
            // condenseInvalidMaterial
            return;
        }
        int requiredAmount = condenseMaterialsManager.getRecipes().get(material);
        int count = heldItem.getAmount();
        if (count < requiredAmount){
            // condenseNotEnoughSpecific
            return;
        }

        int condensedBlocks = count / requiredAmount;
        int remainingItems = count % requiredAmount;
        playerInventory.setItemInMainHand(null);
        Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);

        if (condenseMaterialsManager.getGiveBackEmptyMappings().containsKey(material)){
            Material emptyMaterial = condenseMaterialsManager.getGiveBackEmptyMappings().get(material);
            int emptyCount = count - remainingItems;
            ItemUtils.giveItems(player, new ItemStack(emptyMaterial, emptyCount));
        }

        ItemUtils.giveItems(player, new ItemStack(resultMaterial, condensedBlocks));
        ItemUtils.giveItems(player, new ItemStack(material, remainingItems));
    }

    private void condenseSpecificItem(Player player, String itemString) {
        if (!ItemUtils.isValidItemString(itemString)){
            // condenseInvalidMaterial
            return;
        }
        Material material = Material.matchMaterial(itemString);
        if (!condenseMaterialsManager.getRecipes().containsKey(material)){
            // condenseInvalidMaterial
            return;
        }
        ItemStack[] mainInventory = PlayerUtils.getMainInventory(player);
        int count = ItemUtils.countItems(mainInventory, material);
        int requiredAmount = condenseMaterialsManager.getRecipes().get(material);
        if (count < requiredAmount){
            // condenseNotEnoughSpecific
            return;
        }
        player.getInventory().removeItem(new ItemStack(material, count));
        int condensedBlocks = count / requiredAmount;
        int remainingItems = count % requiredAmount;
        Material resultMaterial = condenseMaterialsManager.getResultMaterial(material, false);

        if (condenseMaterialsManager.getGiveBackEmptyMappings().containsKey(material)){
            Material emptyMaterial = condenseMaterialsManager.getGiveBackEmptyMappings().get(material);
            int emptyCount = count - remainingItems;
            ItemUtils.giveItems(player, new ItemStack(emptyMaterial, emptyCount));
        }
        ItemUtils.giveItems(player, new ItemStack(resultMaterial, condensedBlocks));
        ItemUtils.giveItems(player, new ItemStack(material, remainingItems));
    }


    // condenseCondensedAllReversible = Condensed all reversible recipe items in your inventory.
    // condenseCondensedAll = Condensed all items in your inventory.
    // condenseCondensedHand = Condensed all items in your hand.
    // condenseInvalidMaterial = Invalid material.
    // condenseCondensedSpecific = Condensed all <item> in your inventory.
    // condenseNotEnoughSpecific = You don't have enough <item> to condense.
    // condenseCannotBeCondensedDueToMeta = <item> cannot be condensed due to meta.
}
