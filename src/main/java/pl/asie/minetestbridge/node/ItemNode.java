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

package pl.asie.minetestbridge.node;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.backwards.EntityWrapped;
import pl.asie.minetestbridge.backwards.ItemStackWrapped;

public class ItemNode extends Item implements IItemNode {
    private final LuaTable table;
    private final boolean isTool;

    public ItemNode(LuaTable table) {
        this.table = table;
        this.isTool = table.get("type").optjstring("").equals("tool");
        setCreativeTab(MinetestBridge.tab);
        setMaxStackSize(table.get("stack_max").optint(Items.AIR.getItemStackLimit()));

        if (isTool) {
            setMaxDamage(16383);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        LuaValue func = table.get("on_place");
        if (func.isfunction()) {
            LuaValue result = func.call(new ItemStackWrapped(player.getHeldItem(hand), (s) -> player.setHeldItem(hand, s)), new EntityWrapped(player), MinetestBridge.toMtPointedThing(pos, facing));
            if (result instanceof ItemStackWrapped) {
                player.setHeldItem(hand, ((ItemStackWrapped) result).getStack());
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public LuaTable getLuaTable() {
        return table;
    }

    // ItemBlockNode/ItemNode-shared

    @Override
    public int getItemStackLimit() {
        LuaValue value = table.get("stack_max");
        if (value.isint()) {
            return Math.min(super.getItemStackLimit(), value.toint());
        } else {
            return super.getItemStackLimit();
        }
    }

    @Override
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return getTranslationKey();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return getTranslationKey();
    }

    // ItemNode-specific

    @Override
    public String getTranslationKey() {
        LuaValue value = table.get("description");
        if (value.isstring()) {
            return value.tojstring();
        } else {
            return getRegistryName().toString();
        }
    }
}
