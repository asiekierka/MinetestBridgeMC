package pl.asie.minetestbridge.backwards;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestLib;

public class BackwardsBlockItemPopulator {
    public static void populate(LuaValue globals) {
/*        for (Block b : ForgeRegistries.BLOCKS) {
            LuaTable table = LuaValue.tableOf();

            globals.get("core").get("register_node").invoke(LuaValue.valueOf(MinetestBridge.asMtName(b.getRegistryName())), table);
        }

        for (Item i : ForgeRegistries.ITEMS) {
            if (Block.getBlockFromItem(i) != Blocks.AIR) continue;

            LuaTable table = LuaValue.tableOf();

            table.set("description", LuaValue.valueOf(i.getUnlocalizedName()));
            table.set("stack_max", LuaValue.valueOf(i.getItemStackLimit()));

            globals.get("core").get("register_item").invoke(LuaValue.valueOf(MinetestBridge.asMtName(i.getRegistryName())), table);
        } */
// TODO
    }
}
