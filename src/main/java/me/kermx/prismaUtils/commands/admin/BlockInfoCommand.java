package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockInfoCommand extends BaseCommand {

    public BlockInfoCommand() {
        super("prismautils.command.blockinfo", false, "/blockinfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock != null && targetBlock.getType() != Material.AIR) {
            player.sendMessage(Component.text("Block Info:", NamedTextColor.GREEN));

            // Send Type
            player.sendMessage(Component.text("Type: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, targetBlock.getType().name(), targetBlock.getType().name());

            // Send Hardness
            player.sendMessage(Component.text("Hardness: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, String.valueOf(targetBlock.getType().getHardness()),
                    String.valueOf(targetBlock.getType().getHardness()));

            // Send Blast Resistance
            player.sendMessage(Component.text("Blast Resistance: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, String.valueOf(targetBlock.getType().getBlastResistance()),
                    String.valueOf(targetBlock.getType().getBlastResistance()));

            // Send Location
            player.sendMessage(Component.text("Location: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, targetBlock.getLocation().toString(),
                    targetBlock.getLocation().toString());

            // Send Data
            player.sendMessage(Component.text("Data: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, targetBlock.getBlockData().getAsString(),
                    targetBlock.getBlockData().getAsString());
        } else {
            player.sendMessage(Component.text("You must be looking at a block to use this command!", NamedTextColor.RED));
        }
        return true;
    }

    private void sendCopyMessage(Player player, String label, String value) {
        Component message = Component.text(label + value)
                .clickEvent(ClickEvent.copyToClipboard(value))
                .hoverEvent(HoverEvent.showText(Component.text("Click to copy!")));
        player.sendMessage(message);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
