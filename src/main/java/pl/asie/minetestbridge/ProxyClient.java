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
