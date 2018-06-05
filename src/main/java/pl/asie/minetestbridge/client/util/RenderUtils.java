/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.minetestbridge.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.ITransformation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.util.EntityUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class RenderUtils {
	private static RenderItem renderItem;

	private RenderUtils() {

	}

	public static BakedQuad createQuad(Vector3f from, Vector3f to, @Nonnull EnumFacing facing,
									   TextureAtlasSprite sprite, int tintIndex, ITransformation transformation) {
		Vector3f fFrom = new Vector3f(from);
		Vector3f fTo = new Vector3f(to);
		EnumFacing.AxisDirection facingDir = facing.getAxisDirection();
		switch (facing.getAxis()) {
			case X:
				fFrom.x = fTo.x = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.x : from.x;
				break;
			case Y:
				fFrom.y = fTo.y = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.y : from.y;
				break;
			case Z:
				fFrom.z = fTo.z = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.z : from.z;
				break;
		}

		return CharsetFaceBakery.INSTANCE.makeBakedQuad(fFrom, fTo, tintIndex, sprite, facing, transformation, true);
	}

	public static float[] calculateUV(Vector3f from, Vector3f to, EnumFacing facing1) {
		if (to.y > 16) {
			from = new Vector3f(from);
			to = new Vector3f(to);
			while (to.y > 16) {
				from.y -= 16;
				to.y -= 16;
			}
		}

		EnumFacing facing = facing1;
		if (facing == null) {
			if (from.y == to.y) {
				facing = EnumFacing.UP;
			} else if (from.x == to.x) {
				facing = EnumFacing.EAST;
			} else if (from.z == to.z) {
				facing = EnumFacing.SOUTH;
			} else {
				return null; // !?
			}
		}

		switch (facing) {
			case DOWN:
				return new float[] {from.x, 16.0F - to.z, to.x, 16.0F - from.z};
			case UP:
				return new float[] {from.x, from.z, to.x, to.z};
			case NORTH:
				return new float[] {16.0F - to.x, 16.0F - to.y, 16.0F - from.x, 16.0F - from.y};
			case SOUTH:
				return new float[] {from.x, 16.0F - to.y, to.x, 16.0F - from.y};
			case WEST:
				return new float[] {from.z, 16.0F - to.y, to.z, 16.0F - from.y};
			case EAST:
				return new float[] {16.0F - to.z, 16.0F - to.y, 16.0F - from.z, 16.0F - from.y};
		}

		return null;
	}

	public static IModel getModel(ResourceLocation location) {
		try {
			return ModelLoaderRegistry.getModel(location);
		} catch (Exception e) {
			MinetestBridge.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
			e.printStackTrace();
			return null;
		}
	}

	private static int getSelectionMask(int y, int x, int z) {
		return 1 << (y * 4 + x * 2 + z);
	}

	private static void drawLine(BufferBuilder worldrenderer, Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2) {
		worldrenderer.pos(x1, y1, z1).endVertex();
		worldrenderer.pos(x2, y2, z2).endVertex();
	}

	private static final int[] selectionMask;

	static {
		selectionMask = new int[6];
		selectionMask[0] = 0x00F;
		selectionMask[1] = 0xF00;

		int lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 0);
		lineMask |= getSelectionMask(1, 1, 0);
		lineMask |= getSelectionMask(0, 0, 0);
		lineMask |= getSelectionMask(2, 0, 0);
		selectionMask[2] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 1);
		lineMask |= getSelectionMask(1, 1, 1);
		lineMask |= getSelectionMask(0, 0, 1);
		lineMask |= getSelectionMask(2, 0, 1);
		selectionMask[3] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 0, 0);
		lineMask |= getSelectionMask(1, 0, 1);
		lineMask |= getSelectionMask(0, 1, 0);
		lineMask |= getSelectionMask(2, 1, 0);
		selectionMask[4] = lineMask;

		lineMask = 0;
		lineMask |= getSelectionMask(1, 1, 0);
		lineMask |= getSelectionMask(1, 1, 1);
		lineMask |= getSelectionMask(0, 1, 1);
		lineMask |= getSelectionMask(2, 1, 1);
		selectionMask[5] = lineMask;
	}

	public static int getSelectionMask(EnumFacing face) {
		return selectionMask[face.ordinal()];
	}

	public static void drawSelectionBoundingBox(AxisAlignedBB box, int lineMask) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

		Vec3d cameraPos = EntityUtils.interpolate(player, partialTicks);
		AxisAlignedBB boundingBox = box.grow(0.002).offset(cameraPos.scale(-1));
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		if ((lineMask & /* getSelectionMask(0, 0, 0) */ 0x001) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(0, 0, 1) */ 0x002) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(0, 1, 0) */ 0x004) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(0, 1, 1) */ 0x008) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(1, 0, 0) */ 0x010) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(1, 0, 1) */ 0x020) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(1, 1, 0) */ 0x040) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(1, 1, 1) */ 0x080) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 0, 0) */ 0x100) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & /* getSelectionMask(2, 0, 1) */ 0x200) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 1, 0) */ 0x400) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & /* getSelectionMask(2, 1, 1) */ 0x800) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static int multiplyColor(int src, int dst) {
		int out = 0;
		for (int i = 0; i < 32; i += 8) {
			out |= ((((src >> i) & 0xFF) * ((dst >> i) & 0xFF) / 0xFF) & 0xFF) << i;
		}
		return out;
	}
}
