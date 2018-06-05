package pl.asie.minetestbridge.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestGroupProxy;

import java.util.*;
import java.util.stream.Collectors;

// TODO: Handle missing items as missingnos rather than empty slots
public final class MinetestRecipeProxy {
    private final List<LuaTable> minetestRecipes = new ArrayList<>();
    private final Map<IRecipe, LuaTable> tableMap = new HashMap<>();

    public static final MinetestRecipeProxy INSTANCE = new MinetestRecipeProxy();

    private MinetestRecipeProxy() {

    }

    public boolean registerRecipe(LuaTable recipe) {
        return minetestRecipes.add(recipe);
    }

    public static boolean matches(ItemStack stack, LuaValue v) {
         if (v.isstring()) {
            String s = v.tojstring();

            if (s.startsWith("group:")) {
                int id = OreDictionary.getOreID(MinetestGroupProxy.toMinecraft(s.substring(6)));
                for (int i : OreDictionary.getOreIDs(stack)) {
                    if (i == id) {
                        return true;
                    }
                }
            } else {
                Item i = ForgeRegistries.ITEMS.getValue(MinetestBridge.asMcName(s));
                if (i != null) {
                    return stack.getItem() == i;
                }
            }
        }

        return false;
    }

    public static ItemStack getStack(LuaValue v) {
        Collection<ItemStack> collection = getStackList(v);
        if (collection.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            return collection.iterator().next();
        }
    }

    public static Collection<ItemStack> getStackList(LuaValue v) {
        if (v.isstring()) {
            String s = v.tojstring();
            if (s.trim().isEmpty()) {
                return Collections.emptyList();
            }

            int count = 1;
            if (s.contains(" ")) {
                String[] sp = s.split(" ");
                s = sp[0];
                try {
                    count = Integer.parseInt(sp[1]);
                } catch (NumberFormatException e) {
                    count = 1;
                }
                count = Math.min(64, count);
            }

            if (s.startsWith("group:")) {
                if (count == 1) {
                    return OreDictionary.getOres(MinetestGroupProxy.toMinecraft(s.substring(6)));
                } else {
                    final int _count = count;
                    return OreDictionary.getOres(MinetestGroupProxy.toMinecraft(s.substring(6)))
                            .stream()
                            .map((st) -> {
                                ItemStack ss = st.copy();
                                ss.setCount(_count);
                                return ss;
                            }).collect(Collectors.toList());
                }
            } else {
                Item i = ForgeRegistries.ITEMS.getValue(MinetestBridge.asMcName(s));
                if (i != null) {
                    return Collections.singletonList(new ItemStack(i, 1, OreDictionary.WILDCARD_VALUE));
                }
            }
        }

        return Collections.emptyList();
    }

    public static Ingredient getIngredient(LuaValue v) {
        if (v.isstring()) {
            String s = v.tojstring();
            if (s.trim().isEmpty()) {
                return Ingredient.EMPTY;
            }

            if (s.startsWith("group:")) {
                return new OreIngredient(MinetestGroupProxy.toMinecraft(s.substring(6)));
            } else {
                Item i = ForgeRegistries.ITEMS.getValue(MinetestBridge.asMcName(s));
                if (i != null) {
                    return Ingredient.fromItem(i);
                }
            }
        }

        return Ingredient.EMPTY;
    }

    public static NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv, LuaTable table) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        LuaTable replacements = table.get("replacements").istable() ? table.get("replacements").checktable() : null;

        for (int i = 0; i < ret.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (replacements != null) {
                for (LuaValue key : replacements.keys()) {
                    if (matches(stack, key)) {
                        stack = getStack(replacements.get(key));
                    }
                }
            }
            if (stack != null) {
                ret.set(i, stack);
            }
        }
        return ret;
    }

    @SubscribeEvent
    public void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        tableMap.clear();

        int i = 0;

        for (LuaTable table : minetestRecipes) {
            String type = "shaped";
            if (table.get("type").isstring()) {
                type = table.get("type").tojstring();
            }

            if ("shaped".equals(type)) {
                event.getRegistry().register(new MinetestShapedRecipe(
                        new ResourceLocation("minetestbridge", "shaped_" + (i++)),
                        table
                ));
            } else if ("shapeless".equals(type)) {
                event.getRegistry().register(new MinetestShapelessRecipe(
                        new ResourceLocation("minetestbridge", "shapeless_" + (i++)),
                        table
                ));
            } else if ("toolrepair".equals(type)) {
                MinetestBridge.logger.warn("Unsupported recipe type 'toolrepair'!");
            } else if ("cooking".equals(type)) {
                // cooktime is ignored for now
                for (ItemStack recipe : getStackList(table.get("recipe"))) {
                    for (ItemStack output : getStackList(table.get("output"))) {
                        FurnaceRecipes.instance().addSmeltingRecipe(recipe, output, 0);
                    }
                }
            } else if ("fuel".equals(type)) {
                MinetestBridge.logger.warn("Unsupported recipe type 'fuel'!");
            }
        }
    }
}
