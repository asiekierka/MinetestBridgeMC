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

package pl.asie.minetestbridge.backwards;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestLib;

public class BackwardsBlockItemPopulator {
    public static void populate(LuaValue globals) {
/*        for (Block b : ForgeRegistries.BLOCKS) {
            LuaTable table = LuaValue.tableOf();

            globals.get("core").get("register_node").invoke(LuaValue.valueOf(MinetestBridge.asMtName(b.getRegistryName())), table);
        }

        for (Item i : ForgeRegistries.ITEMS) {
            if (Block.getBlockFromItem(i) != Blocks.AIR) continue;

            LuaTable table = LuaValue.tableOf();

            table.set("description", LuaValue.valueOf(i.getUnlocalizedName()));
            table.set("stack_max", LuaValue.valueOf(i.getItemStackLimit()));

            globals.get("core").get("register_item").invoke(LuaValue.valueOf(MinetestBridge.asMtName(i.getRegistryName())), table);
        } */
// TODO
    }
}
