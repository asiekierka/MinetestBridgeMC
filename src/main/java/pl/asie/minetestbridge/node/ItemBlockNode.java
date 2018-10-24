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

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;

public class ItemBlockNode extends ItemBlock implements IItemNode {
    private final LuaTable table;

    public ItemBlockNode(Block block, LuaTable table) {
        super(block);
        this.table = table;
        setCreativeTab(MinetestBridge.tab);
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

    // ItemBlockNode-specific
}
