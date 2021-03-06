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

package pl.asie.minetestbridge.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;

public class EntityUtils {
	public static Vec3d interpolate(Entity entity, float partialTicks) {
		return new Vec3d(
				entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
				entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
				entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
		);
	}
}
