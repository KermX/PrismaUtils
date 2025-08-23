package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.MailMessage;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.managers.chat.ChatFilterManager;
import me.kermx.prismaUtils.managers.chat.EmojiManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
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

        switch (subCommand) {
            case "send":
                return handleSendMail(sender, args);
            case "read":
                return handleReadMail(sender, args);
            case "clear":
                return handleClearMail(sender, args);
            default:
                return false;
        }
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

        UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : UUID.randomUUID();

        if (senderUUID.equals(targetPlayer.getUniqueId())) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>You can't send mail to yourself!"
            ));
            return true;
        }

        String senderName = (sender instanceof Player) ? sender.getName() : "Console";

        // Join the message from args
        String originalMessage = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String processedMessage = originalMessage;

        // Process chat filtering and emojis if sender is a player
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Get chat handler from plugin
            if (plugin.getChatHandler() != null) {
                ChatFilterManager filterManager = plugin.getChatHandler().getChatFilterManager();
                EmojiManager emojiManager = plugin.getChatHandler().getEmojiManager();

                // Step 1: Check for chat filter violations FIRST on original message
                // This ensures we filter what the player actually typed
                ChatFilterManager.FilterResult filterResult = filterManager.checkMessage(player, originalMessage);

                if (filterResult.isFiltered()) {
                    // Notify staff and player
                    filterManager.notifyStaff(
                            player,
                            "Mail to " + targetName + ": " + originalMessage,
                            filterResult.getFilterName(),
                            filterResult.getReason(),
                            filterResult.getMatchedText(),
                            filterResult.getMatchedPattern()
                    );
                    filterManager.notifyPlayer(player);
                    return true; // Stop processing if filtered
                }

                // Step 2: Process emojis ONLY after message passes all filters
                EmojiManager.ProcessResult emojiResult = emojiManager.processEmojis(player, originalMessage);

                if (!emojiResult.isSuccess()) {
                    // Notify about emoji permission but continue with original message
                    emojiManager.notifyPermissionDenied(player, emojiResult.getDeniedEmoji());
                } else {
                    // Use processed message with emojis
                    processedMessage = emojiResult.getProcessedMessage();
                }
            }
        }

        // Create mail message with processed content
        MailMessage mailMessage = new MailMessage(senderUUID, senderName, processedMessage);

        UUID targetUUID = targetPlayer.getUniqueId();

        // Get the player data - with lazy loading this will load it if needed
        PlayerData targetData = playerDataManager.getPlayerData(targetUUID);

        // Add the message - this will trigger the change listener automatically
        targetData.addMailMessage(mailMessage);

        sender.sendMessage(TextUtils.deserializeString(
                "<green>Message sent to " + targetName + "!"
        ));

        Player online = targetPlayer.getPlayer();
        if (online != null && online.isOnline()) {
            online.sendMessage(TextUtils.deserializeString(
                    "<green>You received a new mail from " + senderName + "!"
            ));
        }
        return true;
    }

    private boolean handleReadMail(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can read mail!");
            return false;
        }

        if (!sender.hasPermission("prismautils.command.mail.read")) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
            ));
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData.getMailbox().isEmpty()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Your mailbox is empty!"
            ));
            return true;
        }

        player.sendMessage(TextUtils.deserializeString(
                "<green>----- Your Mailbox -----"
        ));
        int index = 1;
        for (MailMessage mailMessage : playerData.getMailbox()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<yellow>#" + index + " <gray>[" + mailMessage.getFormattedTimestamp() + "] <white>" +
                            mailMessage.getSenderName() + ": <green>" + mailMessage.getMessage()
            ));
            index++;
        }
        player.sendMessage(TextUtils.deserializeString(
                "<green>----------------------"
        ));
        player.sendMessage(TextUtils.deserializeString(
                "<gray>Use /mail clear to clear your mailbox!"
        ));
        return true;
    }

    private boolean handleClearMail(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can clear mail!");
            return false;
        }

        if (!sender.hasPermission("prismautils.command.mail.clear")) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
            ));
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        // This will trigger the change listener automatically
        playerData.clearMailbox();

        // No need to explicitly save - the change listener will mark it as dirty

        player.sendMessage(TextUtils.deserializeString(
                "<green>Your mailbox has been cleared!"
        ));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("send", "read", "clear");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return null; // Return null to use Bukkit's default player name completion
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Create a specialized mail notification listener for the target player
     * This could be used if you want to implement special notification behaviors
     */
    private void notifyMailReceived(UUID targetUUID, String senderName) {
        Player online = Bukkit.getPlayer(targetUUID);
        if (online != null && online.isOnline()) {
            online.sendMessage(TextUtils.deserializeString(
                    "<green>You received a new mail from " + senderName + "!"
            ));
        }
    }
}