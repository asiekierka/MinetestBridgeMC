package pl.asie.minetestbridge.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class MinetestShapelessRecipe extends ShapelessOreRecipe implements IMinetestRecipe {
    private final LuaTable table;

    public MinetestShapelessRecipe(ResourceLocation group, LuaTable table) {
        super(group, getIngredients(table), MinetestRecipeProxy.getStack(table.get("output")));
        setRegistryName(group);
        this.table = table;
    }

    private static NonNullList<Ingredient> getIngredients(LuaTable table) {
        NonNullList<Ingredient> ings = NonNullList.create();

        for (LuaValue ingKey : table.get("recipe").checktable().keys()) {
            LuaValue ing = table.get("recipe").checktable().get(ingKey);
            ings.add(MinetestRecipeProxy.getIngredient(ing));
        }

        return ings;
    }

    @Override
    public LuaTable getLuaTable() {
        return table;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting crafting) {
        return MinetestRecipeProxy.getRemainingItems(crafting, table);
    }
}
