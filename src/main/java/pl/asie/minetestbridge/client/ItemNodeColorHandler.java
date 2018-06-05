package pl.asie.minetestbridge.client;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.node.ItemNode;

public class ItemNodeColorHandler implements IItemColor {
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Item i = stack.getItem();
            if (i instanceof ItemNode) {
                LuaTable table = ((ItemNode) i).getLuaTable();
                LuaValue color = table.get("color");
                if (color.isint()) {
                    return color.toint() | 0xFF000000;
                }
            }
        }

        return -1;
    }
}
