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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.minetestbridge.util.TileBase;

public class TileEntityNode extends TileBase implements ITileNode {
    private int param2;

    public TileEntityNode() {

    }

    @Override
    public int getParam2() {
        return param2;
    }

    @Override
    public void setParam2(int val) {
        if (val != param2) {
            param2 = val;
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            markDirty();
        }
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        param2 = ((int) compound.getByte("p2")) & 0xFF;
        if (isClient) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        compound.setByte("p2", (byte) param2);
        return compound;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() == newState.getBlock();
    }
}
