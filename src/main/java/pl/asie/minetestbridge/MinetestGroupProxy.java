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

package pl.asie.minetestbridge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.luaj.vm2.ast.Str;

public class MinetestGroupProxy {
    private static final BiMap<String, String> minetestToMinecraft;

    static {
        minetestToMinecraft = HashBiMap.create();
        minetestToMinecraft.put("stick", "stickWood");
    }

    public static String toMinecraft(String s) {
        return minetestToMinecraft.getOrDefault(s, s);
    }

    public static String toMinetest(String s) {
        return minetestToMinecraft.inverse().getOrDefault(s, s);
    }
}
