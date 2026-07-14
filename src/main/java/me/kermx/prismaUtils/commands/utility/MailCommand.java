package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.chat.ChatFilterService;
import me.kermx.prismaUtils.managers.playerdata.MailMessage;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.playerdata.PlayerDataManager;
import me.kermx.prismaUtils.managers.chat.ChatFilterManager;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MailCommand extends BaseCommand {
    private final PlayerDataManager playerDataManager;
    private final PrismaUtils plugin;

    public MailCommand(PrismaUtils plugin) {
        super("prismautils.command.mail", true, "/mail <send|read|clear> [player] [message]");
        this.playerDataManager = plugin.getPlayerDataManager();
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "send" -> handleSendMail(sender, args);
            case "read" -> handleReadMail(sender);
            case "clear" -> handleClearMail(sender);
            default -> false;
        };
    }

    private boolean handleSendMail(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return false;
        }

        if (!sender.hasPermission("prismautils.command.mail.send")) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
            ));
            return true;
        }

        String targetName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(targetName);

        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage
            ));
            return true;
        }

        UUID senderUUID = (sender instanceof Player player) ? player.getUniqueId() : UUID.randomUUID();

        if (senderUUID.equals(targetPlayer.getUniqueId())) {
            sender.sendMessage(TextUtils.deserializeString("<red>You can't send mail to yourself!"));
            return true;
        }

        String senderName = (sender instanceof Player player) ? player.getName() : "Console";
        String originalMessage = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        String processedMessage = originalMessage;

        if (sender instanceof Player player) {
            ChatFilterManager filterManager = plugin.getChatHandler() != null
                    ? plugin.getChatHandler().getChatFilterManager()
                    : null;

            if (filterManager != null) {
                ChatFilterService.Decision decision = filterManager.checkMessage(
                        player,
                        originalMessage,
                        ChatFilterService.Channel.COMMAND_OTHER
                );

                if (decision.blocked()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        filterManager.notifyStaff(player, "Mail to " + targetName + ": " + originalMessage, decision);
                        filterManager.notifyPlayer(player);
                    });
                    return true;
                }

                if (decision.flagged()) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            filterManager.notifyStaff(player, "Mail to " + targetName + ": " + originalMessage, decision)
                    );
                }
            }
        }

        MailMessage mailMessage = new MailMessage(senderUUID, senderName, processedMessage);

        UUID targetUUID = targetPlayer.getUniqueId();
        PlayerData targetData = playerDataManager.getPlayerData(targetUUID);
        targetData.addMailMessage(mailMessage);

        sender.sendMessage(TextUtils.deserializeString("<green>Message sent to " + targetName + "!"));

        Player online = targetPlayer.getPlayer();
        if (online != null && online.isOnline()) {
            online.sendMessage(TextUtils.deserializeString("<green>You received a new mail from " + senderName + "!"));
        }

        return true;
    }

    private boolean handleReadMail(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can read mail!");
            return false;
        }

        if (!sender.hasPermission("prismautils.command.mail.read")) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
            ));
            return true;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData.getMailbox().isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<red>Your mailbox is empty!"));
            return true;
        }

        player.sendMessage(TextUtils.deserializeString("<green>----- Your Mailbox -----"));
        int index = 1;
        for (MailMessage mailMessage : playerData.getMailbox()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<yellow>#" + index + " <gray>[" + mailMessage.getFormattedTimestamp() + "] <white>"
                            + mailMessage.getSenderName() + ": <green>" + mailMessage.getMessage()
            ));
            index++;
        }
        player.sendMessage(TextUtils.deserializeString("<green>----------------------"));
        player.sendMessage(TextUtils.deserializeString("<gray>Use /mail clear to clear your mailbox!"));
        return true;
    }

    private boolean handleClearMail(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can clear mail!");
            return false;
        }

        if (!sender.hasPermission("prismautils.command.mail.clear")) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
            ));
            return true;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        playerData.clearMailbox();

        player.sendMessage(TextUtils.deserializeString("<green>Your mailbox has been cleared!"));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("send", "read", "clear");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return null;
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}