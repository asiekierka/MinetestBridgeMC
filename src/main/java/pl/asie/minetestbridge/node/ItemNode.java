package pl.asie.minetestbridge.node;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.backwards.EntityWrapped;
import pl.asie.minetestbridge.backwards.ItemStackWrapped;

public class ItemNode extends Item implements IItemNode {
    private final LuaTable table;

    public ItemNode(LuaTable table) {
        this.table = table;
        setCreativeTab(MinetestBridge.tab);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        LuaValue func = table.get("on_place");
        if (func.isfunction()) {
            LuaValue result = func.call(new ItemStackWrapped(player.getHeldItem(hand), (s) -> player.setHeldItem(hand, s)), new EntityWrapped(player), MinetestBridge.toMtPointedThing(pos, facing));
            if (result instanceof ItemStackWrapped) {
                player.setHeldItem(hand, ((ItemStackWrapped) result).getStack());
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public LuaTable getLuaTable() {
        return table;
    }

    // ItemBlockNode/ItemNode-shared

    @Override
    public int getItemStackLimit() {
        LuaValue value = table.get("stack_max");
        if (value.isint()) {
            return Math.min(super.getItemStackLimit(), value.toint());
        } else {
            return super.getItemStackLimit();
        }
    }

    @Override
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return getTranslationKey();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return getTranslationKey();
    }

    // ItemNode-specific

    @Override
    public String getTranslationKey() {
        LuaValue value = table.get("description");
        if (value.isstring()) {
            return value.tojstring();
        } else {
            return getRegistryName().toString();
        }
    }
}
