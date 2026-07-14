package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.config.RewardsConfigManager;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RandomRewardCommand extends BaseCommand {

    private static final int MAX_REWARDS = 100;

    public RandomRewardCommand() {
        super("prismautils.command.randomreward", true, "/randomreward give <player> <list> [count]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Player <white>" + args[1] + " <red>is not online."));
            return true;
        }

        RewardsConfigManager rewardsConfig = ConfigManager.getInstance().getRewardsConfig();
        RewardsConfigManager.RewardList list = rewardsConfig.getRewardList(args[2]);
        if (list == null) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Unknown reward list: <white>" + args[2] +
                            "<red>. Available: <white>" + String.join(", ", rewardsConfig.getRewardListNames())));
            return true;
        }

        int count = 1;
        if (args.length >= 4) {
            try {
                count = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TextUtils.deserializeString(
                        "<red>Count must be a number."));
                return true;
            }
            if (count < 1) {
                sender.sendMessage(TextUtils.deserializeString("<red>Count must be at least 1."));
                return true;
            }
            if (count > MAX_REWARDS) {
                sender.sendMessage(TextUtils.deserializeString(
                        "<red>Count may not exceed <white>" + MAX_REWARDS + "<red>."));
                return true;
            }
        }

        int dispatched = 0;
        for (int i = 0; i < count; i++) {
            RewardsConfigManager.RewardEntry entry = list.pickWeighted();
            if (entry == null) {
                continue;
            }
            String command = entry.command()
                    .replace("{player}", target.getName())
                    .replace("{amount}", String.valueOf(count));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            dispatched++;
        }

        sender.sendMessage(TextUtils.deserializeString(
                "<green>Gave <white>" + dispatched + " <green>reward(s) from <white>" + list.getName() +
                        " <green>to <white>" + target.getName() + "<green>."));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("give"), args[0]);
        }
        if (args.length == 2) {
            return filter(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3) {
            return filter(ConfigManager.getInstance().getRewardsConfig().getRewardListNames(), args[2]);
        }
        if (args.length == 4) {
            return filter(List.of("1", "3", "5", "10"), args[3]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
