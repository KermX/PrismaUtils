package me.kermx.prismaUtils.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DisabledCraftingRecipesManager {

    public void removeConfiguredRecipes(){
        List<Material> materialsToRemove = new ArrayList<>();

        for (String material : ConfigManager.getInstance().disabledCraftingRecipes){
            Material mat = Material.getMaterial(material);
            if (mat != null){
                materialsToRemove.add(mat);
            } else {
                Bukkit.getLogger().warning("Material " + material + " is not valid!");
            }
        }

        List<NamespacedKey> keysToRemove = new ArrayList<>();
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()){
            Recipe recipe = recipeIterator.next();
            Material resultType = recipe.getResult().getType();

            if (materialsToRemove.contains(resultType)) {
                switch (recipe) {
                    case ShapedRecipe shaped -> keysToRemove.add(shaped.getKey());
                    case ShapelessRecipe shapeless -> keysToRemove.add(shapeless.getKey());
                    case FurnaceRecipe furnace -> keysToRemove.add(furnace.getKey());
                    case BlastingRecipe blasting -> keysToRemove.add(blasting.getKey());
                    case SmokingRecipe smoking -> keysToRemove.add(smoking.getKey());
                    case CampfireRecipe campfire -> keysToRemove.add(campfire.getKey());
                    case StonecuttingRecipe stonecutting -> keysToRemove.add(stonecutting.getKey());
                    default -> {
                    }
                }
            }
        }

        for (NamespacedKey key : keysToRemove) {
            Bukkit.removeRecipe(key);
        }

    }
}
