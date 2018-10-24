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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.minetestbridge.node.BlockNode;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class TransformingBakedModel implements IBakedModel {
    protected final IBakedModel parent;
    private final ITransformation[] transformations;

    public TransformingBakedModel(IBakedModel parent, ITransformation[] transformations) {
        this.parent = parent;
        this.transformations = transformations;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        // TODO: cache you, cache me
        if (state instanceof IExtendedBlockState) {
            int i = ((IExtendedBlockState) state).getValue(BlockNode.PARAM2) % MathHelper.smallestEncompassingPowerOfTwo(transformations.length);
            ITransformation transformation = i >= transformations.length ? transformations[0] : transformations[i];
            List<BakedQuad> quads = parent.getQuads(state, side == null ? null : transformation.rotate(side), rand);

            List<BakedQuad> newQuads = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                newQuads.add(
                        ModelTransformer.transform(quad,
                                ModelTransformer.IVertexTransformer.transform(transformation)
                        )
                );
            }

            return newQuads;
        } else {
            List<BakedQuad> quads = parent.getQuads(state, side, rand);
            return quads;
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return parent.getOverrides();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return parent.getItemCameraTransforms();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> pair = parent.handlePerspective(cameraTransformType);
        if (pair.getLeft() == parent) {
            return Pair.of(this, pair.getRight());
        } else {
            return pair;
        }
    }
}
