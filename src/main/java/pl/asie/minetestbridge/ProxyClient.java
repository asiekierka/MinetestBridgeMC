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

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.minetestbridge.client.ItemNodeColorHandler;
import pl.asie.minetestbridge.client.ModelRegistry;
import pl.asie.minetestbridge.client.TextureRegistry;
import pl.asie.minetestbridge.node.ItemNode;

public class ProxyClient extends ProxyCommon {
    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TextureRegistry());
        MinecraftForge.EVENT_BUS.register(new ModelRegistry());
    }

    @SubscribeEvent
    public void onItemColor(ColorHandlerEvent.Item event) {
        ItemNodeColorHandler itemNodeColorHandler = new ItemNodeColorHandler();

        for (Item i : ForgeRegistries.ITEMS) {
            if (i instanceof ItemNode) {
                event.getItemColors().registerItemColorHandler(itemNodeColorHandler, i);
            }
        }
    }
}
