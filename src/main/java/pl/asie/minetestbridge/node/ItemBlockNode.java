package pl.asie.minetestbridge.node;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;

public class ItemBlockNode extends ItemBlock implements IItemNode {
    private final LuaTable table;

    public ItemBlockNode(Block block, LuaTable table) {
        super(block);
        this.table = table;
        setCreativeTab(MinetestBridge.tab);
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

    // ItemBlockNode-specific
}
