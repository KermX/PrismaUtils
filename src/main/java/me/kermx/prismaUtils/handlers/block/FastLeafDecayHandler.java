package me.kermx.prismaUtils.handlers.block;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FastLeafDecayHandler implements Listener {

    private final PrismaUtils plugin;
    private static final EnumSet<BlockFace> NEIGHBORS = EnumSet.of(
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    );
    private static final long DELAY_TICKS_AFTER_BREAK = 8L;
    private static final long DELAY_TICKS_AFTER_DECAY = 4L;
    private static final long RUNNER_PERIOD_TICKS = 2L;

    private final Set<BlockKey> pending = ConcurrentHashMap.newKeySet();

    private volatile BukkitTask runner;

    public FastLeafDecayHandler(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Block old = event.getBlock();

        if (!Tag.LOGS.isTagged(old.getType())) return;

        enqueueNeighborLeaves(old);
        startRunnerIfNeeded(DELAY_TICKS_AFTER_BREAK);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLeafDecay(LeavesDecayEvent event) {
        Block old = event.getBlock();

        enqueueNeighborLeaves(old);
        startRunnerIfNeeded(DELAY_TICKS_AFTER_DECAY);
    }

    private void enqueueNeighborLeaves(Block origin) {
        for (BlockFace face : NEIGHBORS) {
            Block b = origin.getRelative(face);

            if (!Tag.LEAVES.isTagged(b.getType())) continue;

            BlockData data = b.getBlockData();
            if (!(data instanceof Leaves leaves)) continue;

            if (leaves.isPersistent()) continue;

            if (leaves.getDistance() < 7) continue;

            pending.add(BlockKey.from(b));
        }
    }

    private void startRunnerIfNeeded(long initialDelay) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> startRunnerIfNeeded(initialDelay));
            return;
        }

        if (runner != null) return;

        long delay = Math.max(1L, initialDelay);
        runner = Bukkit.getScheduler().runTaskTimer(plugin, this::tickOne, delay, RUNNER_PERIOD_TICKS);
    }

    private void tickOne() {
        if (pending.isEmpty()) {
            stopRunner();
            return;
        }

        Iterator<BlockKey> it = pending.iterator();
        while (it.hasNext()) {
            BlockKey key = it.next();
            it.remove();

            World world = Bukkit.getWorld(key.worldId());
            if (world == null) continue;

            Block block = world.getBlockAt(key.x(), key.y(), key.z());
            if (!Tag.LEAVES.isTagged(block.getType())) continue;

            BlockData data = block.getBlockData();
            if (!(data instanceof Leaves leaves)) continue;

            if (leaves.isPersistent()) continue;
            if (leaves.getDistance() < 7) continue;

            LeavesDecayEvent decayEvent = new LeavesDecayEvent(block);
            Bukkit.getPluginManager().callEvent(decayEvent);
            if (decayEvent.isCancelled()) continue;

            block.breakNaturally();
            return;
        }
    }

    private void stopRunner() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::stopRunner);
            return;
        }

        BukkitTask task = runner;
        runner = null;
        if (task != null) task.cancel();
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
        static BlockKey from(Block b) {
            return new BlockKey(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
        }
    }
}
