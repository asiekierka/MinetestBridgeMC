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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

public class EntityWrapped extends LuaTable {
    private final Entity entity;

    public EntityWrapped(Entity entity) {
        this.entity = entity;
        LuaProxy.reflect(this, this);
    }

    @LuaMethod
    public LuaValue is_player(LuaValue wrap) {
        return LuaValue.valueOf(entity instanceof EntityPlayer);
    }

    @LuaMethod
    public LuaValue get_player_name(LuaValue wrap) {
        return LuaValue.valueOf(entity.getName());
    }

    @LuaMethod
    public LuaValue get_player_velocity(LuaValue wrap) {
        return LuaValue.tableOf(new LuaValue[] {
                LuaValue.valueOf(entity.motionX * 20),
                LuaValue.valueOf(entity.motionY * 20),
                LuaValue.valueOf(entity.motionZ * 20)
        });
    }

    @LuaMethod
    public LuaValue get_player_control(LuaValue wrap) {
        // TODO
        LuaValue table = LuaValue.tableOf();
        table.set("jump", LuaValue.valueOf(false));
        table.set("right", LuaValue.valueOf(false));
        table.set("left", LuaValue.valueOf(false));
        table.set("up", LuaValue.valueOf(false));
        table.set("down", LuaValue.valueOf(false));
        table.set("LMB", LuaValue.valueOf(false));
        table.set("RMB", LuaValue.valueOf(false));
        table.set("aux1", LuaValue.valueOf(false));
        table.set("sneak", LuaValue.valueOf(entity.isSneaking()));
        return table;
    }

    @LuaMethod
    public LuaValue get_player_control_bits(LuaValue wrap) {
        // TODO
        return LuaValue.valueOf(entity.isSneaking() ? 64 : 0);
    }
}
