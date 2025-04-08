package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.MailMessage;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
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

    public MailCommand(PrismaUtils plugin) {
        super("prismautils.command.mail", true, "/mail <send|read|clear> [player] [message]");
        this.playerDataManager = plugin.getPlayerDataManager();

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
        }

        String senderName = (sender instanceof Player) ? sender.getName() : "Console";

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        MailMessage mailMessage = new MailMessage(senderUUID, senderName, message);

        UUID targetUUID = targetPlayer.getUniqueId();
        PlayerData targetData = playerDataManager.getPlayerData(targetUUID);

        if (targetData == null) {
            playerDataManager.loadPlayerData(targetUUID);
            targetData = playerDataManager.getPlayerData(targetUUID);
        }

        targetData.addMailMessage(mailMessage);
        playerDataManager.savePlayerData(targetUUID);

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

        Player player = (Player) sender;
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        playerData.clearMailbox();
        playerDataManager.savePlayerData(player.getUniqueId());

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
}
