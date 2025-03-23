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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UncondenseCommand extends BaseCommand {

    private final CondenseMaterialsManager condenseMaterialsManager;

    public UncondenseCommand() {
        super("prismautils.command.uncondense", false, "/uncondense [all|hand|item]");
        this.condenseMaterialsManager = new CondenseMaterialsManager();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {

        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();

        if (args.length == 0 || args[0].equalsIgnoreCase("all")) {
            uncondenseInventory(player);
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().uncondenseMessage,
                            Placeholder.component("from", Component.text("all reversible items")),
                            Placeholder.component("to", Component.text("their base materials")))
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("hand")) {
            ItemStack itemInHand = inventory.getItemInMainHand();
            if (itemInHand.getType().isAir()) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseUncondenseNotHoldingAnyItemMessage)
                );
                return true;
            }
            uncondenseItem(player, itemInHand.getType());
            return true;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().incorrectUsageMessage)
            );
            return true;
        }

        uncondenseItem(player, material);
        return true;
    }

    private void uncondenseInventory(Player player) {
        Map<Material, Material> reversibleMappings = condenseMaterialsManager.getReversibleMaterialMappings(true);
        for (Material blockMaterial : reversibleMappings.keySet()) {
            int uncondensedItemCount = uncondenseItem(player, blockMaterial);
            if (uncondensedItemCount > 0) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(blockMaterial))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(condenseMaterialsManager.getReversibleMaterialMappings(false).get(blockMaterial)))))
                );
            }
        }
    }

    private int uncondenseItem(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();
        Map<Material, Material> reversibleMappings = condenseMaterialsManager.getReversibleMaterialMappings(true);
        Map<Material, Integer> reversibleRecipes = condenseMaterialsManager.getReversibleRecipes();

        if (!reversibleMappings.containsKey(material) || !reversibleRecipes.containsKey(reversibleMappings.get(material))) {
            return 0; // No message, return 0 to indicate no uncondensing occurred
        }

        Material resultMaterial = reversibleMappings.get(material);
        int outputAmount = reversibleRecipes.get(resultMaterial);
        int count = ItemUtils.countItems(inventory.getStorageContents(), material);

        if (count < 1) {
            return 0; // No message, return 0 to indicate no uncondensing occurred
        }

        int totalUncondensedItems = count * outputAmount;
        int stacks = totalUncondensedItems / 64;
        int leftovers = totalUncondensedItems % 64;
        List<ItemStack> itemsToGive = new ArrayList<>();

        inventory.removeItem(new ItemStack(material, count));

        for (int i = 0; i < stacks; i++) {
            itemsToGive.add(new ItemStack(resultMaterial, 64));
        }
        if (leftovers > 0) {
            itemsToGive.add(new ItemStack(resultMaterial, leftovers));
        }

        for (ItemStack itemStack : itemsToGive) {
            ItemUtils.giveItems(player, itemStack);
        }

        player.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                        Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(material))),
                        Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(resultMaterial))))
        );

        return totalUncondensedItems; // Return the number of uncondensed items
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return super.onTabCompleteExecute(sender, args);
        }

        if (args.length == 1) {
            Set<String> suggestionSet = new HashSet<>();

            Map<Material, Material> reversibleMappings = condenseMaterialsManager.getReversibleMaterialMappings(true);
            for (Material material : reversibleMappings.keySet()) {
                suggestionSet.add(material.name());
            }

            List<String> suggestions = new ArrayList<>(suggestionSet);
            return suggestions;
        }

        return super.onTabCompleteExecute(sender, args);
    }

}