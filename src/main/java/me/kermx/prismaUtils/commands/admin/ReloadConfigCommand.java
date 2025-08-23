package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadConfigCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public ReloadConfigCommand(PrismaUtils plugin) {
        super("prismautils.command.reload", true, "/prismautilsreload");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        long startTime = System.currentTimeMillis();

        sender.sendMessage(TextUtils.deserializeString("<yellow>Reloading PrismaUtils configuration..."));

        try {
            // Reload all configurations
            ConfigManager.getInstance().reloadAll();

            // Reload chat handler if it exists
            if (plugin.getChatHandler() != null) {
                plugin.getChatHandler().reload();
            }

            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(TextUtils.deserializeString(
                    "<green>Configuration reloaded successfully in " + duration + "ms!"
            ));

        } catch (Exception e) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Error reloading configuration: " + e.getMessage()
            ));
            plugin.getLogger().severe("Error reloading configuration:");
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}