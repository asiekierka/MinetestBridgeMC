package pl.asie.minetestbridge.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestLib;
import pl.asie.minetestbridge.client.util.NodeboxBakedModel;
import pl.asie.minetestbridge.node.BlockNode;
import pl.asie.minetestbridge.node.ItemNode;

import java.util.HashMap;
import java.util.Map;

public class ModelRegistry implements IStateMapper {
    public static String getTextureName(String owner, LuaValue value) {
        if (value.isstring()) {
            return TextureRegistry.toMcLoc(owner, value.checkjstring()).toString();
        } else if (value.istable() && value.get("name").isstring()) {
            return TextureRegistry.toMcLoc(owner, value.get("name").checkjstring()).toString();
        } else {
            return "missingno";
        }
    }

    public static IModel getModel(ResourceLocation location) {
        try {
            return ModelLoaderRegistry.getModel(location);
        } catch (Exception e) {
            MinetestBridge.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
        for (LuaValue key : MinetestLib.getRegistry("items").keys()) {
            LuaValue value = MinetestLib.getRegistry("items").get(key);
            if (key.isstring() && value.istable()) {
                ResourceLocation loc = MinetestBridge.asMcName(key.tojstring());
                Item item = ForgeRegistries.ITEMS.getValue(loc);
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(loc, "inventory"));
            }
        }
    }

    private static void add(ImmutableMap.Builder<String, String> builder, String key, String value) {
        builder.put(key, value);
        builder.put('#'+key, value);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        for (LuaValue key : MinetestLib.getRegistry("items").keys()) {
            LuaValue value = MinetestLib.getRegistry("items").get(key);
            if (key.isstring() && value.istable()) {
                if (value.get("inventory_image").isstring()) {
                    String owner = key.tojstring().split(":")[0];
                    ImmutableList.Builder<ResourceLocation> textures = new ImmutableList.Builder<>();
                    textures.add(TextureRegistry.toMcLoc(owner, value.get("inventory_image").checkjstring()));
                    if (value.get("inventory_overlay").isstring()) {
                        textures.add(TextureRegistry.toMcLoc(owner, value.get("inventory_overlay").checkjstring()));
                    }

                    ResourceLocation loc = MinetestBridge.asMcName(key.tojstring());
                    ModelResourceLocation mloc = new ModelResourceLocation(loc, "inventory");
                    ItemLayerModel model = new ItemLayerModel(textures.build());

                    event.getModelRegistry().putObject(mloc,
                            model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
                }
            }
        }

        for (LuaValue key : MinetestLib.getRegistry("nodes").keys()) {
            LuaValue value = MinetestLib.getRegistry("nodes").get(key);
            if (key.isstring() && value.istable()) {
                if (value.get("tiles").istable()) {
                    String owner = key.tojstring().split(":")[0];
                    ResourceLocation loc = MinetestBridge.asMcName(key.tojstring());
                    LuaValue tiles = value.get("tiles");

                    IModel model = null;
                    IBakedModel bm = null;
                    String drawtype = value.get("drawtype").optjstring("normal");

                    if (drawtype.equals("plantlike")) {
                        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
                        add(map, "cross", getTextureName(owner, tiles.get(1)));
                        model = getModel(new ResourceLocation("minecraft", "block/cross"))
                                .retexture(map.build());
                    } else {
                        bm = new NodeboxBakedModel(owner, value, ModelRotation.X0_Y0, ModelLoader.defaultTextureGetter());
                    }

                    if (model != null && bm == null) {
                        bm = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    }

                    if (bm != null) {
                        event.getModelRegistry().putObject(new ModelResourceLocation(loc, "normal"), bm);
                        event.getModelRegistry().putObject(new ModelResourceLocation(loc, "inventory"), bm);
                    }
                }
            }
        }
    }

    private static final Map<String, ITransformation[]> types = new HashMap<>();

    static {
        types.put("none", new ITransformation[]{ModelRotation.X0_Y0});
    }

    private ITransformation[] getTransformations(LuaValue value) {
        String paramtype2 = value.get("paramtype2").optjstring("none");
        if (!types.containsKey(paramtype2)) paramtype2 = "none";
        return types.get(paramtype2);
    }

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
        ImmutableMap.Builder<IBlockState, ModelResourceLocation> builder = new ImmutableMap.Builder<>();
        for (IBlockState state : blockIn.getBlockState().getValidStates()) {
            builder.put(state, new ModelResourceLocation(blockIn.getRegistryName(), "normal"));
        }
        return builder.build();
    }
}
