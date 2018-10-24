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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public abstract class TileBase extends TileEntity {
    public abstract void readNBTData(NBTTagCompound compound, boolean isClient);

    public abstract NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient);

    public boolean hasDataPacket() {
        return true;
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return hasDataPacket() ? new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), writeNBTData(new NBTTagCompound(), true)) : null;
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
        compound = writeNBTData(compound, true);
        return compound;
    }

    @Override
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (pkt != null) {
            readNBTData(pkt.getNbtCompound(), true);
        }
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readNBTData(compound, world != null && world.isRemote);
    }

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        return writeNBTData(compound, false);
    }
}
