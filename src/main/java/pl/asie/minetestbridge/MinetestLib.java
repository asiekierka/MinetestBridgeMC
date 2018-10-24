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

package pl.asie.minetestbridge;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.apache.commons.codec.binary.Base64;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import pl.asie.minetestbridge.backwards.ItemStackWrapped;
import pl.asie.minetestbridge.node.BlockNode;
import pl.asie.minetestbridge.node.ITileNode;
import pl.asie.minetestbridge.node.TileEntityNode;
import pl.asie.minetestbridge.recipe.MinetestRecipeProxy;
import pl.asie.minetestbridge.util.LuaField;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MinetestLib extends ZeroArgFunction {
    @LuaField
    public static MinetestSettingsLib settings = new MinetestSettingsLib();

    public static String getAliased(String name) {
        LuaTable aliases = MinetestBridge.globals.get("core").get("registered_aliases").checktable();
        while (aliases.get(name).isstring()) {
            name = aliases.get(name).tojstring();
        }
        return name;
    }

    public static LuaTable getRegistry(String name) {
        return MinetestBridge.globals.get("core").get("registered_"+name).checktable();
    }

    @LuaMethod
    public LuaValue is_singleplayer() {
        return LuaBoolean.FALSE;
    }

    @LuaMethod
    public LuaValue get_craft_result(LuaValue value) {
        // TODO: return two, work
        LuaValue table = LuaValue.tableOf();
        table.set("item", new ItemStackWrapped(ItemStack.EMPTY));
        table.set("time", 0);
        table.set("replacements", LuaValue.tableOf());
        return table;
    }

    @LuaMethod
    public LuaValue log(String type, String text) {
        if (text == null) {
            type = "info";
            text = type;
        }

        if ("warning".equals(type) || "deprecated".equals(type)) {
            MinetestBridge.logger.warn("[Minetest] " + text);
        } else {
            MinetestBridge.logger.info("[Minetest] " + text);
        }

        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_mapgen_setting(String key) {
        // TODO
        if ("chunksize".equals(key)) {
            return LuaValue.valueOf("6");
        } else if ("mg_name".equals(key)) {
            return LuaValue.valueOf("v6");
        } else {
            return LuaValue.NIL;
        }
    }

    @LuaMethod
    public LuaValue get_decoration_id(String name) {
        // TODO
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue set_gen_notify(LuaValue a, LuaValue b) {
        // TODO
        return null;
    }

    @LuaMethod
    public LuaValue register_craft(LuaValue craft) {
        if (craft.istable()) {
            MinetestRecipeProxy.INSTANCE.registerRecipe(craft.checktable());
        }
        return null;
    }

    @LuaMethod
    public LuaValue register_biome(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue register_ore(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue register_decoration(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue clear_registered_biomes(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue clear_registered_ores(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue clear_registered_decorations(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue register_item_raw(LuaValue table) {
        MinetestBridge.logger.info("Registering item " + table.get("name").optjstring("???"));
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue unregister_item_raw(LuaValue table) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue register_alias_raw(String from, String to) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_current_modname() {
        MinetestMod mod = MinetestBridge.getCurrentMod();
        return mod != null ? LuaValue.valueOf(mod.getName()) : LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_player_privs(String name) {
        return LuaValue.tableOf(); // TODO
    }

    @LuaMethod
    public LuaValue get_last_run_mod() {
        MinetestMod mod = MinetestBridge.getLastRunMod();
        return mod != null ? LuaValue.valueOf(mod.getName()) : LuaValue.valueOf("*builtin*");
    }

    @LuaMethod
    public LuaValue get_builtin_path() {
        return LuaValue.valueOf(MinetestBridge.getBuiltinDir().getAbsolutePath() + File.separator);
    }

    @LuaMethod
    public LuaValue get_worldpath() {
        return LuaValue.valueOf(MinetestBridge.getBaseDir().getAbsolutePath() + File.separator);
    }

    @LuaMethod
    public LuaValue get_modpath(String modname) {
        MinetestMod mod = MinetestBridge.getMod(modname);
        if (mod != null) {
            return LuaValue.valueOf(mod.getDir().getAbsolutePath() + File.separator);
        }
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_node(LuaValue pos) {
        LuaValue result = get_node_or_nil(pos);
        if (result.isnil()) {
            result = new LuaTable();
            result.set("name", LuaValue.valueOf("ignore"));
            result.set("param1", LuaValue.valueOf(0));
            result.set("param2", LuaValue.valueOf(0));
        }
        return result;
    }

    @LuaMethod
    public LuaValue get_node_or_nil(LuaValue pos) {
        BlockPos mcPos = MinetestBridge.toMcPos(pos);
        World w = MinetestBridge.getCurrentWorld();
        if (w.isBlockLoaded(mcPos)) {
            IBlockState state = w.getBlockState(mcPos);
            LuaTable result = new LuaTable();
            result.set("name", LuaValue.valueOf(MinetestBridge.asMtName(state.getBlock().getRegistryName())));
            result.set("param1", LuaValue.valueOf(
                    (w.getLightFor(EnumSkyBlock.BLOCK, mcPos) & 0xF)
                    | ((w.getLight(mcPos) & 0xF) << 4)
            ));
            int param2 = 0;
            if (state.getBlock() instanceof BlockNode) {
                TileEntity tile = w.getTileEntity(mcPos);
                if (tile instanceof ITileNode) {
                    param2 = ((ITileNode) tile).getParam2();
                }
            }
            result.set("param2", LuaValue.valueOf(param2));
            return result;
        } else {
            return LuaValue.NIL;
        }
    }

    @LuaMethod
    public LuaValue check_for_falling(LuaValue pos) {
        // TODO: Correct?
        BlockPos mcPos = MinetestBridge.toMcPos(pos);
        World w = MinetestBridge.getCurrentWorld();
        w.neighborChanged(mcPos, w.getBlockState(mcPos).getBlock(), mcPos);
        return null;
    }

    @LuaMethod
    public LuaValue swap_node(LuaValue pos, LuaValue node) {
        BlockPos mcPos = MinetestBridge.toMcPos(pos);
        World w = MinetestBridge.getCurrentWorld();
        IBlockState state = w.getBlockState(mcPos);
        String currName = MinetestBridge.asMtName(state.getBlock().getRegistryName());
        if (!currName.equals(node.get("name").optjstring("ignore"))) {
            MinetestBridge.logger.warn("Swapping nodes of different types is not supported yet!");
        }

        LuaValue param2 = node.get("param2");
        if (param2.isnumber() && state.getBlock() instanceof BlockNode) {
            TileEntity tile = w.getTileEntity(mcPos);
            if (tile instanceof ITileNode) {
                ((ITileNode) tile).setParam2(param2.toint());
            }
        }

        return null;
    }

    @LuaMethod
    public LuaValue set_node(LuaValue pos, LuaValue node) {
        MinetestBridge.logger.warn("TODO: set_node");
        return null;
    }

    @LuaMethod
    public LuaValue add_node(LuaValue pos, LuaValue node) {
        return set_node(pos, node);
    }

    @LuaMethod
    public LuaValue remove_node(LuaValue pos) {
        BlockPos mcPos = MinetestBridge.toMcPos(pos);
        MinetestBridge.getCurrentWorld().setBlockToAir(mcPos);
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue delete_area(LuaValue pos1, LuaValue pos2) {
        World w = MinetestBridge.getCurrentWorld();
        BlockPos.getAllInBoxMutable(MinetestBridge.toMcPos(pos1), MinetestBridge.toMcPos(pos2)).forEach(
                w::setBlockToAir
        );
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_timeofday() {
        int l = (int) MinetestBridge.getCurrentWorld().getWorldTime();
        return LuaValue.valueOf((double) ((l + 6000) % 24000) / 24000.0);
    }

    @LuaMethod
    public LuaValue get_gametime() {
        long l = MinetestBridge.getCurrentWorld().getTotalWorldTime();
        return LuaValue.valueOf((int) (l / 20));
    }

    @LuaMethod
    public LuaValue get_day_count() {
        long l = MinetestBridge.getCurrentWorld().getTotalWorldTime();
        return LuaValue.valueOf((int) (l / 24000));
    }

    @LuaMethod
    public LuaValue set_timeofday(LuaValue val) {
        double d = val.checkdouble();
        int l = ((int) (d * 24000.0) + 18000) % 24000;
        MinetestBridge.getCurrentWorld().setWorldTime(l);
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_us_time() {
        return LuaValue.valueOf((double) (System.currentTimeMillis() * 1000));
    }

    @Override
    public LuaValue call() {
        LuaValue lib = LuaProxy.reflect(this);
        env.set("core", lib);
        return lib;
    }
}
