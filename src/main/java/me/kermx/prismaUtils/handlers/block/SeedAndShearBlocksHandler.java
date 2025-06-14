package me.kermx.prismaUtils.handlers.block;

import me.kermx.prismaUtils.integrations.ProtectionService;
import me.kermx.prismaUtils.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Wall;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SeedAndShearBlocksHandler implements Listener {

    private final ProtectionService protectionService;

    private final Map<Material, Material> seedTransformMap = new HashMap<>();
    private final Map<Material, Material> shearTransformMap = new HashMap<>();

    private static final Material SEED_MATERIAL = Material.WHEAT_SEEDS;
    private static final Material SHEAR_MATERIAL = Material.SHEARS;

    public SeedAndShearBlocksHandler(ProtectionService protectionService) {
        this.protectionService = protectionService;
        registerTransformations();
    }

    public void registerTransformations() {
        seedTransformMap.put(Material.DIRT, Material.GRASS_BLOCK);
        seedTransformMap.put(Material.PODZOL, Material.GRASS_BLOCK);
        seedTransformMap.put(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        seedTransformMap.put(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS);
        seedTransformMap.put(Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB);
        seedTransformMap.put(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
        seedTransformMap.put(Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS);
        seedTransformMap.put(Material.STONE_BRICK_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS);
        seedTransformMap.put(Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB);
        seedTransformMap.put(Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL);

        shearTransformMap.put(Material.MOSSY_COBBLESTONE, Material.COBBLESTONE);
        shearTransformMap.put(Material.MOSSY_COBBLESTONE_STAIRS, Material.COBBLESTONE_STAIRS);
        shearTransformMap.put(Material.MOSSY_COBBLESTONE_SLAB, Material.COBBLESTONE_SLAB);
        shearTransformMap.put(Material.MOSSY_COBBLESTONE_WALL, Material.COBBLESTONE_WALL);
        shearTransformMap.put(Material.MOSSY_STONE_BRICKS, Material.STONE_BRICKS);
        shearTransformMap.put(Material.MOSSY_STONE_BRICK_STAIRS, Material.STONE_BRICK_STAIRS);
        shearTransformMap.put(Material.MOSSY_STONE_BRICK_SLAB, Material.STONE_BRICK_SLAB);
        shearTransformMap.put(Material.MOSSY_STONE_BRICK_WALL, Material.STONE_BRICK_WALL);
    }

    @EventHandler
    public void onBlockRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!seedTransformMap.containsKey(block.getType()) && !shearTransformMap.containsKey(block.getType())){
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Material itemType = itemInHand.getType();

        if (itemType == Material.AIR) return;
        if (itemType != SHEAR_MATERIAL && itemType != SEED_MATERIAL) return;

        if (protectionService.isLocationProtected(player, block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (itemType == SEED_MATERIAL) {
            Material original = block.getType();
            Material target = seedTransformMap.get(original);
            if (target == null) return;

            transformBlock(block, target);
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            event.setCancelled(true);
        } else if (itemType == SHEAR_MATERIAL) {
            Material original = block.getType();
            Material target = shearTransformMap.get(original);
            if (target == null) return;

            transformBlock(block, target);

            ItemUtils.damageItem(itemInHand, 1);

            event.setCancelled(true);
        }
    }

    private void transformBlock(Block block, Material newMaterial) {
        BlockData originalData = block.getBlockData();
        BlockData newData = Bukkit.createBlockData(newMaterial);

        if (newData instanceof Directional && originalData instanceof Directional) {
            ((Directional) newData).setFacing(((Directional) originalData).getFacing());
        }

        if (newData instanceof Stairs newStairs && originalData instanceof Stairs oldStairs) {
            newStairs.setShape(oldStairs.getShape());
            newStairs.setFacing(oldStairs.getFacing());
            newStairs.setHalf(oldStairs.getHalf());
        }

        if (newData instanceof Slab && originalData instanceof Slab) {
            ((Slab) newData).setType(((Slab) originalData).getType());
        }

        if (newData instanceof Orientable && originalData instanceof Orientable) {
            ((Orientable) newData).setAxis(((Orientable) originalData).getAxis());
        }

        if (newData instanceof Rotatable && originalData instanceof Rotatable) {
            ((Rotatable) newData).setRotation(((Rotatable) originalData).getRotation());
        }

        if (newData instanceof Waterlogged && originalData instanceof Waterlogged) {
            ((Waterlogged) newData).setWaterlogged(((Waterlogged) originalData).isWaterlogged());
        }

        if (newData instanceof Wall newWall && originalData instanceof Wall oldWall) {

            for (BlockFace face : new BlockFace[]{
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST
            }) {
                newWall.setHeight(face, oldWall.getHeight(face));
                newWall.setUp(oldWall.isUp());
            }
        }

        block.setBlockData(newData);
    }

}
