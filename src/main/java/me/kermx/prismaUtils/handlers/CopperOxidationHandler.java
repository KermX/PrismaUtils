package me.kermx.prismaUtils.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CopperOxidationHandler implements Listener {

    private static final int TRANSFORM_RADIUS = 3;
    private static final double TRANSFORM_PROBABILITY = 0.5;
    private final Random random = new Random();

    @EventHandler //MAYBE COME BACK and add area effect clouds continually oxidizing copper
    public void onProjectileHit(ProjectileHitEvent event){
        if (event.getEntityType() == EntityType.POTION){
            ThrownPotion potion = (ThrownPotion) event.getEntity();

            if (potion.getEffects().stream().anyMatch(effect -> effect.getType().equals(PotionEffectType.SPEED))){
                Block middle = event.getHitBlock();

                if (middle == null) return;

                for (int x = -TRANSFORM_RADIUS; x <= TRANSFORM_RADIUS; x++){
                    for (int y = -TRANSFORM_RADIUS; y <= TRANSFORM_RADIUS; y++){
                        for (int z = -TRANSFORM_RADIUS; z <= TRANSFORM_RADIUS; z++){
                            Block currentBlock = middle.getRelative(x, y, z);

                            if (random.nextDouble() < TRANSFORM_PROBABILITY){
                                transformCopperBlock(currentBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    private void transformCopperBlock(Block block){
        BlockData originalData = block.getBlockData();
        BlockData newData = null;

        switch (block.getType()){
            case COPPER_BLOCK -> newData = Bukkit.createBlockData(Material.EXPOSED_COPPER);
            case EXPOSED_COPPER -> newData = Bukkit.createBlockData(Material.WEATHERED_COPPER);
            case WEATHERED_COPPER -> newData = Bukkit.createBlockData(Material.OXIDIZED_COPPER);

            case CUT_COPPER_STAIRS -> newData = Bukkit.createBlockData(Material.EXPOSED_CUT_COPPER_STAIRS);
            case EXPOSED_CUT_COPPER_STAIRS -> newData = Bukkit.createBlockData(Material.WEATHERED_CUT_COPPER_STAIRS);
            case WEATHERED_CUT_COPPER_STAIRS -> newData = Bukkit.createBlockData(Material.OXIDIZED_CUT_COPPER_STAIRS);

            case CUT_COPPER_SLAB -> newData = Bukkit.createBlockData(Material.EXPOSED_CUT_COPPER_SLAB);
            case EXPOSED_CUT_COPPER_SLAB -> newData = Bukkit.createBlockData(Material.WEATHERED_CUT_COPPER_SLAB);
            case WEATHERED_CUT_COPPER_SLAB -> newData = Bukkit.createBlockData(Material.OXIDIZED_CUT_COPPER_SLAB);

            case CUT_COPPER -> newData = Bukkit.createBlockData(Material.EXPOSED_CUT_COPPER);
            case EXPOSED_CUT_COPPER -> newData = Bukkit.createBlockData(Material.WEATHERED_CUT_COPPER);
            case WEATHERED_CUT_COPPER -> newData = Bukkit.createBlockData(Material.OXIDIZED_CUT_COPPER);

            case CHISELED_COPPER -> newData = Bukkit.createBlockData(Material.EXPOSED_CHISELED_COPPER);
            case EXPOSED_CHISELED_COPPER -> newData = Bukkit.createBlockData(Material.WEATHERED_CHISELED_COPPER);
            case WEATHERED_CHISELED_COPPER -> newData = Bukkit.createBlockData(Material.OXIDIZED_CHISELED_COPPER);

            case COPPER_GRATE -> newData = Bukkit.createBlockData(Material.EXPOSED_COPPER_GRATE);
            case EXPOSED_COPPER_GRATE -> newData = Bukkit.createBlockData(Material.WEATHERED_COPPER_GRATE);
            case WEATHERED_COPPER_GRATE -> newData = Bukkit.createBlockData(Material.OXIDIZED_COPPER_GRATE);

            case COPPER_BULB -> newData = Bukkit.createBlockData(Material.EXPOSED_COPPER_BULB);
            case EXPOSED_COPPER_BULB -> newData = Bukkit.createBlockData(Material.WEATHERED_COPPER_BULB);
            case WEATHERED_COPPER_BULB -> newData = Bukkit.createBlockData(Material.OXIDIZED_COPPER_BULB);

            case COPPER_DOOR -> newData = Bukkit.createBlockData(Material.EXPOSED_COPPER_DOOR);
            case EXPOSED_COPPER_DOOR -> newData = Bukkit.createBlockData(Material.WEATHERED_COPPER_DOOR);
            case WEATHERED_COPPER_DOOR -> newData = Bukkit.createBlockData(Material.OXIDIZED_COPPER_DOOR);

            case COPPER_TRAPDOOR -> newData = Bukkit.createBlockData(Material.EXPOSED_COPPER_TRAPDOOR);
            case EXPOSED_COPPER_TRAPDOOR -> newData = Bukkit.createBlockData(Material.WEATHERED_COPPER_TRAPDOOR);
            case WEATHERED_COPPER_TRAPDOOR -> newData = Bukkit.createBlockData(Material.OXIDIZED_COPPER_TRAPDOOR);

            default -> {}
        }

        if (newData != null){
            copyOrientationData(originalData, newData);
            block.setBlockData(newData);
        }
    }

    private void copyOrientationData(BlockData originalData, BlockData newData){
        if (newData instanceof Directional && originalData instanceof Directional) {
            ((Directional) newData).setFacing(((Directional) originalData).getFacing());
        }

        if (newData instanceof Stairs newStairs && originalData instanceof Stairs oldStairs) {
            newStairs.setShape(oldStairs.getShape());
            newStairs.setFacing(oldStairs.getFacing());
            newStairs.setHalf(oldStairs.getHalf());
        }

        if (newData instanceof Slab newSlab && originalData instanceof Slab oldSlab) {
            newSlab.setType(oldSlab.getType());
        }

        if (newData instanceof Orientable newOrient && originalData instanceof Orientable oldOrient) {
            newOrient.setAxis(oldOrient.getAxis());
        }

        if (newData instanceof Rotatable newRot && originalData instanceof Rotatable oldRot) {
            newRot.setRotation(oldRot.getRotation());
        }

        if (newData instanceof Waterlogged newWater && originalData instanceof Waterlogged oldWater) {
            newWater.setWaterlogged(oldWater.isWaterlogged());
        }

        if (newData instanceof Door newDoor && originalData instanceof Door oldDoor) {
            newDoor.setOpen(oldDoor.isOpen());
            newDoor.setHalf(oldDoor.getHalf());
            newDoor.setHinge(oldDoor.getHinge());
            newDoor.setFacing(oldDoor.getFacing());
        }

        if (newData instanceof TrapDoor newTrap && originalData instanceof TrapDoor oldTrap) {
            newTrap.setOpen(oldTrap.isOpen());
            newTrap.setHalf(oldTrap.getHalf());
            newTrap.setFacing(oldTrap.getFacing());
        }

        if (newData instanceof Powerable newPow && originalData instanceof Powerable oldPow){
            newPow.setPowered(oldPow.isPowered());
        }

        if (newData instanceof Lightable newLight && originalData instanceof Lightable oldLight){
            newLight.setLit(oldLight.isLit());
        }
    }

}
