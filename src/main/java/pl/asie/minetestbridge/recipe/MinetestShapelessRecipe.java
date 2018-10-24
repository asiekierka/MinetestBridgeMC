/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of MinetestBridge.
 *
 * MinetestBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MinetestBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MinetestBridge.  If not, see <http://www.gnu.org/licenses/>.
 */

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
