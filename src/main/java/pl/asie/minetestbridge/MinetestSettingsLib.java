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

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

public class MinetestSettingsLib extends LuaTable {
    public MinetestSettingsLib() {
        LuaProxy.reflect(this, this);
    }

    @LuaMethod
    public LuaValue get(LuaValue wrap, String name) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_bool(LuaValue wrap, String name) {
        return LuaValue.valueOf(false);
    }
}
