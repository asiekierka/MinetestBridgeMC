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

package pl.asie.minetestbridge.client;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.node.ItemNode;

public class ItemNodeColorHandler implements IItemColor {
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Item i = stack.getItem();
            if (i instanceof ItemNode) {
                LuaTable table = ((ItemNode) i).getLuaTable();
                LuaValue color = table.get("color");
                if (color.isint()) {
                    return color.toint() | 0xFF000000;
                }
            }
        }

        return -1;
    }
}
