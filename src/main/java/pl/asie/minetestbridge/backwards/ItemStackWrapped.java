package pl.asie.minetestbridge.backwards;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestLib;
import pl.asie.minetestbridge.recipe.MinetestRecipeProxy;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemStackWrapped extends LuaTable {
    @Nullable
    private final Consumer<ItemStack> stackReplacer;
    private final ItemStack stack;

    public ItemStackWrapped(ItemStack stack) {
        this(stack, null);
    }

    public ItemStackWrapped(LuaValue wrap) {
        this.stackReplacer = null;
        if (wrap instanceof ItemStackWrapped) {
            this.stack = ((ItemStackWrapped) wrap).getStack();
        } else {
            this.stack = MinetestRecipeProxy.getStack(wrap);
        }
        LuaProxy.reflect(this, this);
    }

    public ItemStackWrapped(ItemStack stack, Consumer<ItemStack> stackReplacer) {
        this.stack = stack;
        this.stackReplacer = stackReplacer;
        LuaProxy.reflect(this, this);
    }

    public ItemStack getStack() {
        return stack;
    }

    @LuaMethod
    public LuaValue is_empty(LuaValue wrap) {
        return LuaValue.valueOf(stack.isEmpty());
    }

    @LuaMethod
    public LuaValue get_name(LuaValue wrap) {
        return LuaValue.valueOf(MinetestBridge.asMtName(stack.getItem().getRegistryName()));
    }

    @LuaMethod
    public LuaValue get_count(LuaValue wrap) {
        return LuaValue.valueOf(stack.getCount());
    }

    @LuaMethod
    public LuaValue set_count(LuaValue wrap, LuaValue count) {
        stack.setCount(count.checkint());
        return is_empty(wrap);
    }

    @LuaMethod
    public LuaValue clear(LuaValue wrap) {
        stack.setCount(0);
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_definition(LuaValue wrap) {
        try {
            return MinetestLib.getRegistry("items").get(MinetestBridge.asMtName(stack.getItem().getRegistryName()));
        } catch (LuaError e) {
            return LuaValue.NIL;
        }
    }

    @LuaMethod
    public LuaValue get_stack_max(LuaValue wrap) {
        return LuaValue.valueOf(stack.getMaxStackSize());
    }

    @LuaMethod
    public LuaValue get_free_space(LuaValue wrap) {
        return LuaValue.valueOf(stack.getMaxStackSize() - stack.getCount());
    }

    @LuaMethod
    public LuaValue take_item(LuaValue wrap, LuaValue count) {
        int c = count.optint(1);
        return new ItemStackWrapped(stack.splitStack(c));
    }

    @LuaMethod
    public LuaValue peek_item(LuaValue wrap, LuaValue count) {
        int c = count.optint(1);
        ItemStack ns = stack.copy();
        ns.setCount(Math.min(c, ns.getCount()));
        return new ItemStackWrapped(ns);
    }
}
