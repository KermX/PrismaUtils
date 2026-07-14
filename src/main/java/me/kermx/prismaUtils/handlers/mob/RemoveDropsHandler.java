package me.kermx.prismaUtils.handlers.mob;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RemoveDropsHandler implements Listener {

    private static final Map<EntityType, Set<Material>> DROP_BLACKLIST;

    static {
        var map = new EnumMap<EntityType, Set<Material>>(EntityType.class);

        // Piglins
        Set<Material> goldWeapons = EnumSet.of(
                Material.GOLDEN_SWORD, Material.GOLDEN_AXE, Material.GOLDEN_SPEAR);
        map.put(EntityType.PIGLIN,           goldWeapons);
        map.put(EntityType.PIGLIN_BRUTE,     goldWeapons);
        map.put(EntityType.ZOMBIFIED_PIGLIN, goldWeapons);

        // Skeletons
        Set<Material> bows = EnumSet.of(Material.BOW);
        map.put(EntityType.SKELETON, bows);
        map.put(EntityType.STRAY,    bows);
        map.put(EntityType.PARCHED,  bows);
        map.put(EntityType.BOGGED,   bows);

        // Wither skeletons
        map.put(EntityType.WITHER_SKELETON, EnumSet.of(Material.STONE_SWORD));

        // Illagers
        map.put(EntityType.VINDICATOR, EnumSet.of(Material.IRON_AXE));
        map.put(EntityType.PILLAGER,   EnumSet.of(Material.CROSSBOW));

        DROP_BLACKLIST = Collections.unmodifiableMap(map);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            Set<Material> equipped = equippedMaterials(entity.getEquipment());
            if (!equipped.isEmpty()) {
                event.getDrops().removeIf(item -> item != null && equipped.contains(item.getType()));
            }
        }

        Set<Material> blacklist = DROP_BLACKLIST.get(entity.getType());
        if (blacklist != null) {
            event.getDrops().removeIf(item -> item != null && blacklist.contains(item.getType()));
        }
    }

    private Set<Material> equippedMaterials(EntityEquipment equip) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        if (equip == null) return materials;

        addIfReal(materials, equip.getHelmet());
        addIfReal(materials, equip.getChestplate());
        addIfReal(materials, equip.getLeggings());
        addIfReal(materials, equip.getBoots());
        addIfReal(materials, equip.getItemInMainHand());
        addIfReal(materials, equip.getItemInOffHand());
        return materials;
    }

    private void addIfReal(Set<Material> set, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            set.add(item.getType());
        }
    }
}
