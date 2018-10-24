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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.util.LuaProxy;
import pl.asie.minetestbridge.util.UnlistedPropertyInteger;

import javax.annotation.Nullable;
import java.util.List;

public class BlockNode extends Block implements IBlockNode, ITileEntityProvider {
    public static final UnlistedPropertyInteger PARAM2 = new UnlistedPropertyInteger("param2", 0, 255);
    private final LuaTable table;
    private final boolean rendersLikeAir;
    private final boolean isGlasslike, normalCube;
    private final boolean sunlightPropagates, walkable;

    public BlockNode(LuaTable table) {
        super(materialFromTable(table));
        this.table = table;
        this.lightValue = table.get("light_source").optint(0);
        this.rendersLikeAir = table.get("drawtype").optjstring("").equals("airlike");
        this.isGlasslike = table.get("drawtype").optjstring("").startsWith("glasslike");
        this.normalCube = table.get("drawtype").optjstring("normal").equals("normal")
            || (table.get("drawtype").optjstring("normal").equals("nodebox") && (!table.get("nodebox").istable() || table.get("nodebox").get("type").optjstring("regular").equals("regular")));
        this.sunlightPropagates = table.get("sunlight_propagates").optboolean(false);
        this.walkable = table.get("walkable").optboolean(true);
        setCreativeTab(MinetestBridge.tab);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{PARAM2});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityNode) {
            return ((IExtendedBlockState) state).withProperty(PARAM2, ((TileEntityNode) tile).getParam2());
        } else {
            return ((IExtendedBlockState) state).withProperty(PARAM2, 0);
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!walkable) return;

        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (!walkable) return NULL_AABB;

        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return normalCube;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return normalCube;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return normalCube && !sunlightPropagates;
    }

    private static Material materialFromTable(LuaTable table) {
        return Material.ROCK;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return isGlasslike ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public LuaTable getLuaTable() {
        return table;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return rendersLikeAir ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
    }

    @Override
    public String getTranslationKey() {
        LuaValue value = table.get("description");
        if (value.isstring()) {
            return value.tojstring();
        } else {
            return getRegistryName().toString();
        }
    }

    @Override
    public String getLocalizedName() {
        return getTranslationKey();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return table.get("paramtype2").isstring();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNode();
    }
}
