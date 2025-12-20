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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UncondenseCommand extends BaseCommand {

    private final CondenseMaterialsManager cmm;
    private final CondenseManager condenseManager;

    public UncondenseCommand() {
        super("prismautils.command.uncondense", false, "/uncondense [all|hand|item]");
        this.cmm = new CondenseMaterialsManager();
        this.condenseManager = new CondenseManager(cmm);
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {

        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();

        if (args.length == 0 || args[0].equalsIgnoreCase("all")) {
            List<CondenseManager.Conversion> conversions = condenseManager.uncondense(player, null);

            if (conversions.isEmpty()) {
                player.sendMessage(TextUtils.deserializeString("You don't have any items to uncondense."));
                return true;
            }

            for (CondenseManager.Conversion c : conversions) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                                Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                                Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
                );
            }

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

            List<CondenseManager.Conversion> conversions = condenseManager.uncondense(player, itemInHand.getType());
            if (conversions.isEmpty()) return true;

            CondenseManager.Conversion c = conversions.getFirst();
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                            Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                            Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
            );
            return true;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().incorrectUsageMessage)
            );
            return true;
        }

        List<CondenseManager.Conversion> conversions = condenseManager.uncondense(player, material);
        if (conversions.isEmpty()) return true;

        CondenseManager.Conversion c = conversions.getFirst();
        player.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().condenseMessage,
                        Placeholder.component("from", Component.text(TextUtils.normalizeEnumName(c.from()))),
                        Placeholder.component("to", Component.text(TextUtils.normalizeEnumName(c.to()))))
        );
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return super.onTabCompleteExecute(sender, args);
        }

        if (args.length == 1) {
            Set<String> suggestionSet = new HashSet<>();

            // Suggest blocks (the "condensed" form) from reversible rules.
            Map<Material, Material> baseToBlock = cmm.getReversibleMaterialMappings(false);
            for (Material block : baseToBlock.values()) {
                suggestionSet.add(block.name());
            }

            return new ArrayList<>(suggestionSet);
        }

        return super.onTabCompleteExecute(sender, args);
    }

}