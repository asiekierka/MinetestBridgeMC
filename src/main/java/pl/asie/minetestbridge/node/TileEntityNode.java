package pl.asie.minetestbridge.node;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.minetestbridge.util.TileBase;

public class TileEntityNode extends TileBase {
    private int param2;

    public TileEntityNode() {

    }

    public int getParam2() {
        return param2;
    }

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
