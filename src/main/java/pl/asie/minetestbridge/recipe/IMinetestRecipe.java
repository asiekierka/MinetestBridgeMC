package pl.asie.minetestbridge.recipe;

import net.minecraft.item.crafting.IRecipe;
import org.luaj.vm2.LuaTable;

public interface IMinetestRecipe extends IRecipe {
    LuaTable getLuaTable();
}
