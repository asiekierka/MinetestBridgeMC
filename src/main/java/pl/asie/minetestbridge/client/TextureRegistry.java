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

package pl.asie.minetestbridge.client;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.codec.digest.DigestUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestLib;

import java.io.File;

public class TextureRegistry {
    public static ResourceLocation toMcLoc(String owner, String tex) {
        tex = DigestUtils.md5Hex(tex);
        return new ResourceLocation("minetestbridge:" + tex);
    }

    private void appendTexture(String owner, LuaValue invImage, TextureMap map) {
        LuaValue obj = null;

        if (invImage.istable() && invImage.get("name").isstring()) {
            obj = invImage;
            invImage = invImage.get("name");
        }

        if (invImage.isstring()) {
            ResourceLocation textureName = toMcLoc(owner, invImage.tojstring());
            map.setTextureEntry(new TextureMinetestSprite(textureName.toString(),
                    MinetestBridge.getModDir(), MinetestBridge.getLoadedMods(), invImage.tojstring(), obj));
        }
    }

    private void appendTextureTable(String owner, LuaValue texTable, TextureMap map) {
        if (texTable.istable()) {
            for (LuaValue key : texTable.checktable().keys()) {
                appendTexture(owner, texTable.checktable().get(key), map);
            }
        }
    }

    private void appendTextures(String owner, LuaValue node, TextureMap map) {
        appendTexture(owner, node.get("inventory_image"), map);
        appendTexture(owner, node.get("inventory_overlay"), map);
        appendTexture(owner, node.get("wield_image"), map);
        appendTexture(owner, node.get("wield_overlay"), map);

        appendTextureTable(owner, node.get("textures"), map);

        appendTextureTable(owner, node.get("tiles"), map);
        appendTextureTable(owner, node.get("overlay_tiles"), map);
        appendTextureTable(owner, node.get("special_tiles"), map);
    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        for (LuaValue key : MinetestLib.getRegistry("nodes").keys()) {
            LuaValue value = MinetestLib.getRegistry("nodes").get(key);
            if (key.isstring() && value.istable()) {
                appendTextures(key.tojstring().split(":")[0], value, event.getMap());
            }
        }

        for (LuaValue key : MinetestLib.getRegistry("items").keys()) {
            LuaValue value = MinetestLib.getRegistry("items").get(key);
            if (key.isstring() && value.istable()) {
                appendTextures(key.tojstring().split(":")[0], value, event.getMap());
            }
        }
    }
}
