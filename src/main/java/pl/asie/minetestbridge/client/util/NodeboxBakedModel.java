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

package pl.asie.minetestbridge.client.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.model.ITransformation;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.client.ModelRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NodeboxBakedModel extends BaseBakedModel {
    private TextureAtlasSprite[] sprites;
    private List<BakedQuad>[] quads;

    public NodeboxBakedModel(String owner, LuaValue table, ITransformation transformation, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        // TODO: Optimize memory usage
        quads = (List<BakedQuad>[]) new List[7];
        for (int i = 0; i < 7; i++) quads[i] = new ArrayList<>();

        LuaTable tilesTable = table.get("tiles").checktable();
        LuaValue nodeboxTable = table.get("node_box");

        TextureAtlasSprite[] tilesSprite = new TextureAtlasSprite[tilesTable.length()];
        for (int i = 0; i < tilesSprite.length; i++) {
            System.out.println(ModelRegistry.getTextureName(owner, tilesTable.get(i + 1)));
            tilesSprite[i] = textureGetter.apply(new ResourceLocation(ModelRegistry.getTextureName(owner, tilesTable.get(i + 1))));
        }

        switch (tilesSprite.length) {
            case 1:
            default:
                sprites = new TextureAtlasSprite[] {
                    tilesSprite[0], tilesSprite[0], tilesSprite[0], tilesSprite[0], tilesSprite[0], tilesSprite[0]
                };
                break;
            case 2:
                sprites = new TextureAtlasSprite[] {
                        tilesSprite[0], tilesSprite[0], tilesSprite[1], tilesSprite[1], tilesSprite[1], tilesSprite[1]
                };
                break;
            case 3:
                sprites = new TextureAtlasSprite[] {
                        tilesSprite[1], tilesSprite[0], tilesSprite[2], tilesSprite[2], tilesSprite[2], tilesSprite[2]
                };
                break;
            case 6:
                sprites = new TextureAtlasSprite[] {
                        tilesSprite[1], tilesSprite[0], tilesSprite[5], tilesSprite[4], tilesSprite[3], tilesSprite[2]
                };
                break;
        }

        String type = nodeboxTable.istable() ? nodeboxTable.get("type").optjstring("regular") : "regular";
        if ("regular".equals(type)) {
            addCube(transformation, new AxisAlignedBB(0,0,0,16,16,16), 0);
        } else if ("fixed".equals(type)) {
            LuaValue fixed = nodeboxTable.get("fixed");
            if (fixed.istable() && fixed.length() >= 1 && fixed.get(1).istable()) {
                for (int i = 0; i < fixed.length(); i++) {
                    addCube(transformation, fixed.get(i + 1), 0);
                }
            } else {
                addCube(transformation, fixed, 0);
            }
        } else {
            MinetestBridge.logger.warn("Unsupported nodebox type " + type + "!");
            addCube(transformation, new AxisAlignedBB(0,0,0,16,16,16), 0);
        }

        addDefaultBlockTransforms();
    }

    private void addCube(ITransformation t, LuaValue box, int tintIndex) {
        if (box.length() == 6) {
            addCube(t, new AxisAlignedBB(
                    (box.get(1).checkdouble() + 0.5) * 16,
                    (box.get(2).checkdouble() + 0.5) * 16,
                    (box.get(3).checkdouble() + 0.5) * 16,
                    (box.get(4).checkdouble() + 0.5) * 16,
                    (box.get(5).checkdouble() + 0.5) * 16,
                    (box.get(6).checkdouble() + 0.5) * 16
            ), tintIndex);
        }
    }

    private void addCube(ITransformation t, AxisAlignedBB box, int tintIndex) {
        Vector3f from = new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ);
        Vector3f to = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);

        for (EnumFacing facing : EnumFacing.VALUES) {
            int list = 6;
            switch (facing) {
                case DOWN:
                    if (box.minY <= 0.0D) {
                        list = 0;
                    }
                    break;
                case UP:
                    if (box.maxY >= 1.0D) {
                        list = 1;
                    }
                    break;
                case NORTH:
                    if (box.minZ <= 0.0D) {
                        list = 2;
                    }
                    break;
                case SOUTH:
                    if (box.maxZ >= 1.0D) {
                        list = 3;
                    }
                    break;
                case WEST:
                    if (box.minX <= 0.0D) {
                        list = 4;
                    }
                    break;
                case EAST:
                    if (box.maxX <= 0.0D) {
                        list = 5;
                    }
                    break;
            }

            quads[list].add(RenderUtils.createQuad(from, to, facing, sprites[facing.ordinal()], tintIndex, t));
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return quads[side == null ? 6 : side.ordinal()];
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprites[0];
    }
}
