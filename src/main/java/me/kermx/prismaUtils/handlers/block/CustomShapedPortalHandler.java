package me.kermx.prismaUtils.handlers.block;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomShapedPortalHandler implements Listener {

    private final PrismaUtils plugin;

    private static final int MAX_INTERIOR_BLOCKS = 441;
    private final BlockData portalX;
    private final BlockData portalZ;

    public CustomShapedPortalHandler(PrismaUtils plugin) {
        this.plugin = plugin;

        this.portalX = makePortalData(Axis.X);
        this.portalZ = makePortalData(Axis.Z);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPortalIgnite(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null) return;

        Material itemType = event.getItem().getType();
        if (itemType != Material.FLINT_AND_STEEL && itemType != Material.FIRE_CHARGE) {
            return;
        }

        Block clicked = event.getClickedBlock();

        if (clicked.getType() != Material.OBSIDIAN) return;

        BlockFace face = event.getBlockFace();
        Block target = clicked.getRelative(face);

        if (target.getType() != Material.AIR) return;

        Player player = event.getPlayer();

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.getType() == Material.AIR) {
                target.setType(Material.FIRE, false);
            }

            boolean created = tryCreateCustomPortalAt(target, player);

            if (!created && target.getType() == Material.FIRE) {
                target.setType(Material.AIR, false);
            }
        });

        if (player.getGameMode() != GameMode.CREATIVE) {
            if (itemType == Material.FLINT_AND_STEEL) {
                event.getItem().damage(1, player);
            } else if (itemType == Material.FIRE_CHARGE) {
                consumeOne(event.getItem(), player);
            }
        }
    }

    private boolean tryCreateCustomPortalAt(Block origin, Player creator) {
        if (origin.getType() != Material.FIRE) return false;

        if (tryCreateInPlane(origin, Axis.X, portalX, creator)) return true;
        return tryCreateInPlane(origin, Axis.Z, portalZ, creator);
    }

    private boolean tryCreateInPlane(Block origin, Axis axis, BlockData portalData, Player creator) {
        Plane plane = Plane.forAxis(axis);

        FloodResult result = floodInterior(origin, plane);
        if (!result.enclosed) return false;

        int size = result.interior.size();
        if (size == 0 || size > MAX_INTERIOR_BLOCKS) return false;

        List<BlockState> states = new ArrayList<>(size);
        for (Block b : result.interior) {
            b.setBlockData(portalData, false);
            states.add(b.getState());
        }

        PortalCreateEvent pce = new PortalCreateEvent(
                states,
                origin.getWorld(),
                creator,
                PortalCreateEvent.CreateReason.FIRE
        );
        Bukkit.getPluginManager().callEvent(pce);

        if (pce.isCancelled()) {
            for (BlockState st : states) {
                st.getBlock().setType(Material.AIR, false);
            }
            return false;
        }

        return true;
    }

    private FloodResult floodInterior(Block start, Plane plane) {
        World world = start.getWorld();

        ArrayDeque<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        boolean enclosed = true;

        while (!queue.isEmpty()) {
            if (visited.size() > MAX_INTERIOR_BLOCKS) {
                return new FloodResult(false, visited);
            }

            Block current = queue.removeFirst();

            if (!isInteriorType(current.getType())) {
                return new FloodResult(false, Set.of());
            }

            for (BlockFace face : plane.neighbors) {
                Block neighbor = world.getBlockAt(
                        current.getX() + face.getModX(),
                        current.getY() + face.getModY(),
                        current.getZ() + face.getModZ()
                );

                Material m = neighbor.getType();

                if (m == Material.OBSIDIAN) {
                    continue;
                }

                if (!isInteriorType(m)) {
                    enclosed = false;
                    continue;
                }

                if (visited.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        return new FloodResult(enclosed, visited);
    }

    private static boolean isInteriorType(Material type) {
        return type == Material.AIR || type == Material.FIRE || type == Material.NETHER_PORTAL;
    }

    private static BlockData makePortalData(Axis axis) {
        BlockData data = Bukkit.createBlockData(Material.NETHER_PORTAL);
        Orientable o = (Orientable) data;
        o.setAxis(axis);
        return o;
    }

    private record FloodResult(boolean enclosed, Set<Block> interior) {}

    private enum Plane {
        XY(Axis.X, new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}),
        ZY(Axis.Z, new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN});

        final Axis axis;
        final BlockFace[] neighbors;

        Plane(Axis axis, BlockFace[] neighbors) {
            this.axis = axis;
            this.neighbors = neighbors;
        }

        static Plane forAxis(Axis axis) {
            return axis == Axis.X ? XY : ZY;
        }
    }

    private static void consumeOne(ItemStack hand, Player player) {
        int amt = hand.getAmount();
        if (amt <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(amt - 1);
            player.getInventory().setItemInMainHand(hand);
        }
    }
}
