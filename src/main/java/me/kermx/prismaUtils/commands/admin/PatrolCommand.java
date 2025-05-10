package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PatrolCommand extends BaseCommand {
    private static final Map<UUID, PatrolSession> activeSessions = new HashMap<>();
    private static final String PATROL_EXCLUDED_PERMISSION = "prismautils.patrol.excluded";

    public PatrolCommand() {
        super("prismautils.command.patrol", false, "/patrol");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player staffMember = (Player) sender;

        // Check if staff member is in vanish
        if (!PlayerUtils.isVanished(staffMember)) {
            staffMember.sendMessage(TextUtils.deserializeString("<red>You must be vanished to use patrol."));
            return true;
        }

        UUID staffId = staffMember.getUniqueId();

        // Get or create a patrol session for this staff member
        PatrolSession session = activeSessions.computeIfAbsent(staffId, id -> new PatrolSession());

        // Get the next player to patrol
        Player targetPlayer = session.getNextPlayer(staffMember);

        if (targetPlayer == null) {
            staffMember.sendMessage(TextUtils.deserializeString("<red>No suitable players found to patrol."));
            return true;
        }

        // Teleport to the player
        staffMember.teleport(targetPlayer.getLocation());
        staffMember.sendMessage(TextUtils.deserializeString(
                "<green>Patrolling player: <white>" + targetPlayer.getName() +
                        "<green> (" + session.getVisitedCount() + "/" + session.getTotalPlayers() + ")"));

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion for this command
        return new ArrayList<>();
    }

    // Inner class to handle patrol session state
    private static class PatrolSession {
        private final List<UUID> playersToVisit = new ArrayList<>();
        private final Set<UUID> visitedPlayers = new HashSet<>();
        private int totalPlayers = 0;

        // Gets the next player to visit, refreshing the list if needed
        public Player getNextPlayer(Player staffMember) {
            // If we've visited everyone or list is empty, refresh the list
            if (playersToVisit.isEmpty()) {
                refreshPlayerList(staffMember);

                // If still empty after refresh, there are no valid players
                if (playersToVisit.isEmpty()) {
                    return null;
                }
            }

            // Get the next player and move them to visited set
            UUID nextPlayerId = playersToVisit.removeFirst();
            visitedPlayers.add(nextPlayerId);

            Player nextPlayer = PlayerUtils.getOnlinePlayer(nextPlayerId);

            // If player went offline or is no longer valid, try again
            if (nextPlayer == null || !nextPlayer.isOnline()) {
                return getNextPlayer(staffMember);
            }

            return nextPlayer;
        }

        // Refreshes the list of players to visit
        private void refreshPlayerList(Player staffMember) {
            // Clear the list but keep track of who we've visited this cycle
            playersToVisit.clear();

            // Get all online players excluding staff, excluded players, and those in vanish
            List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !player.hasPermission("prismautils.command.patrol"))  // Exclude staff
                    .filter(player -> !player.hasPermission(PATROL_EXCLUDED_PERMISSION))    // Exclude players with excluded permission
                    .filter(player -> !PlayerUtils.isVanished(player))                      // Exclude vanished players
                    .filter(player -> !player.equals(staffMember))                          // Exclude the staff member
                    .collect(Collectors.toList());

            totalPlayers = onlinePlayers.size();

            // If we've visited everyone in this cycle, reset and start over
            if (visitedPlayers.size() >= totalPlayers) {
                visitedPlayers.clear();
            }

            // Add players we haven't visited yet
            for (Player player : onlinePlayers) {
                UUID playerId = player.getUniqueId();
                if (!visitedPlayers.contains(playerId)) {
                    playersToVisit.add(playerId);
                }
            }

            // Shuffle to make the order random
            Collections.shuffle(playersToVisit);
        }

        // Get count of visited players for display
        public int getVisitedCount() {
            return visitedPlayers.size();
        }

        // Get total players for display
        public int getTotalPlayers() {
            return totalPlayers;
        }
    }
}