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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.List;

public class MinetestShapedRecipe extends ShapedOreRecipe implements IMinetestRecipe {
    private final LuaTable table;

    public MinetestShapedRecipe(ResourceLocation group, LuaTable table) {
        super(group, MinetestRecipeProxy.getStack(table.get("output")), getShapedPrimer(table));
        setRegistryName(group);
        this.table = table;
    }

    private static CraftingHelper.ShapedPrimer getShapedPrimer(LuaTable table) {
        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();

        int width = 0;
        List<List<Ingredient>> ings = new ArrayList<>();
        for (LuaValue rowKey : table.get("recipe").checktable().keys()) {
            LuaValue row = table.get("recipe").checktable().get(rowKey);
            List<Ingredient> ings1 = new ArrayList<>();
            for (LuaValue ingKey : row.checktable().keys()) {
                LuaValue ing = row.checktable().get(ingKey);
                ings1.add(MinetestRecipeProxy.getIngredient(ing));
            }
            ings.add(ings1);
            width = Math.max(ings1.size(), width);
        }

        primer.width = width;
        primer.height = ings.size();
        primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);
        for (int y = 0; y < ings.size(); y++) {
            List<Ingredient> ings1 = ings.get(y);
            for (int x = 0; x < ings1.size(); x++) {
                primer.input.set(y*width+x, ings1.get(x));
            }
        }
        primer.mirrored = false;

        return primer;
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
