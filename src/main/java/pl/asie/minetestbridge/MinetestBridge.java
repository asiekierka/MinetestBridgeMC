package pl.asie.minetestbridge;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import pl.asie.minetestbridge.backwards.BackwardsBlockItemPopulator;
import pl.asie.minetestbridge.backwards.ItemStackWrapped;
import pl.asie.minetestbridge.client.ModelRegistry;
import pl.asie.minetestbridge.client.TextureRegistry;
import pl.asie.minetestbridge.node.BlockNode;
import pl.asie.minetestbridge.node.ItemBlockNode;
import pl.asie.minetestbridge.node.ItemNode;
import pl.asie.minetestbridge.node.TileEntityNode;
import pl.asie.minetestbridge.recipe.MinetestRecipeProxy;

import java.io.File;
import java.util.*;

@Mod(modid = MinetestBridge.MODID, name = MinetestBridge.NAME, version = MinetestBridge.VERSION)
public class MinetestBridge
{
    static final String MODID = "minetestbridge";
    static final String NAME = "MinetestBridge";
    static final String VERSION = "0.1.0";

    static LuaValue globals;

    public static Logger logger;
    public static CreativeTabs tab;

    private static MinetestMod currMod, lastMod;
    private static File minetestEnvDir, modDir;
    private static List<MinetestMod> modOrder = new ArrayList<>();
    private static Map<String, MinetestMod> modMap = new HashMap<>();

    @SidedProxy(serverSide = "pl.asie.minetestbridge.ProxyCommon", clientSide = "pl.asie.minetestbridge.ProxyClient", modId = MODID)
    private static ProxyCommon proxy;

    public static MinetestMod getCurrentMod() {
        return currMod;
    }

    public static MinetestMod getLastRunMod() {
        return lastMod;
    }

    public static File getBaseDir() {
        return minetestEnvDir;
    }

    public static File getBuiltinDir() {
        return new File(minetestEnvDir, "builtin");
    }

    public static Collection<MinetestMod> getLoadedMods() {
        return Collections.unmodifiableList(modOrder);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        globals = JsePlatform.debugGlobals();
        globals.set("DIR_DELIM", LuaValue.valueOf(File.separator));
        globals.set("INIT", LuaValue.valueOf("game"));
        globals.load(new MinetestLib());
        globals.set("ItemStack", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return new ItemStackWrapped(arg);
            }
        });

        minetestEnvDir = new File(event.getModConfigurationDirectory().getParentFile(), "minetest");
        modDir = new File(minetestEnvDir, "mods");
        if (!minetestEnvDir.exists()) minetestEnvDir.mkdir();
        if (!modDir.exists()) modDir.mkdir();

        logger.info("Loading builtin Lua code");
        MinetestBridge.globals.get("dofile").call(LuaValue.valueOf(new File(getBuiltinDir(), "init.lua").getAbsolutePath()));

        try {
            for (File f : Objects.requireNonNull(modDir.listFiles())) {
                if (f.isDirectory() && new File(f, "init.lua").exists()) {
                    MinetestMod mod = new MinetestMod(f);
                    modMap.put(mod.getName(), mod);
                    logger.info("Found Minetest mod " + mod.getName());
                }
            }

            Set<String> modsAdded = new HashSet<>();
            while (modOrder.size() < modMap.size()) {
                int lastSize = modOrder.size();
                List<MinetestMod> mta = new ArrayList<>();

                for (MinetestMod m : modMap.values()) {
                    if (modsAdded.contains(m.getName())) continue;

                    boolean canAdd = true;
                    for (String s : m.getDependencies()) {
                        if (s.endsWith("?")) {
                            s = s.substring(0, s.length() - 1);
                            if (!modMap.containsKey(s)) {
                                continue;
                            }
                        }

                        if (!modsAdded.contains(s)) {
                            canAdd = false;
                            break;
                        }
                    }

                    if (canAdd) {
                        mta.add(m);
                    }
                }

                mta.sort(Comparator.comparing(MinetestMod::getName));
                modOrder.addAll(mta);
                for (MinetestMod m : mta) {
                    modsAdded.add(m.getName());
                }

                if (modOrder.size() == lastSize) {
                    throw new RuntimeException("Could not resolve dependency graph!");
                }
            }

            for (MinetestMod mod : modOrder) {
                currMod = mod;
                lastMod = mod;
                logger.info("Initializing mod " + mod.getName());
                mod.preInit();
            }
            currMod = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MinecraftForge.EVENT_BUS.register(this);
        tab = new CreativeTabs("minetestbridge") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(Blocks.BEDROCK);
            }
        };

        MinecraftForge.EVENT_BUS.register(MinetestRecipeProxy.INSTANCE);

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntityNode.class, "minetestbridge:node");
        BackwardsBlockItemPopulator.populate(globals);
    }

    private static final Map<LuaValue, BlockNode> blocks = new HashMap<>();

    // TODO: ProxyClient-ify
    public static World getCurrentWorld() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            return DimensionManager.getWorld(0);
        } else {
            return Minecraft.getMinecraft().world;
        }
    }

    public static LuaTable toMtPointedThing(BlockPos pos, EnumFacing facing) {
        LuaTable result = new LuaTable();
        result.set("type", "node");
        result.set("under", toMtPos(pos));
        result.set("above", toMtPos(pos.offset(facing)));
        return result;
    }

    public static LuaTable toMtPointedThing(RayTraceResult trace) {
        LuaTable result = new LuaTable();
        if (trace != null) {
            if (trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                result.set("type", "node");
                result.set("under", toMtPos(trace.getBlockPos()));
                result.set("above", toMtPos(trace.getBlockPos().offset(trace.sideHit)));
            } else {
                result.set("type", LuaValue.valueOf("nothing"));
            }
        } else {
            result.set("type", LuaValue.valueOf("nothing"));
        }
        return result;
    }

    public static BlockPos toMcPos(LuaValue value) {
        return value.istable() ? new BlockPos(value.get("x").checkint(), value.get("y").checkint(), value.get("z").checkint()) : BlockPos.ORIGIN;
    }

    public static LuaTable toMtPos(BlockPos pos) {
        LuaTable result = new LuaTable();
        result.set("x", LuaValue.valueOf(pos.getX()));
        result.set("y", LuaValue.valueOf(pos.getY()));
        result.set("z", LuaValue.valueOf(pos.getZ()));
        return result;
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        blocks.clear();

        for (LuaValue key : MinetestLib.getRegistry("nodes").keys()) {
            LuaValue value = MinetestLib.getRegistry("nodes").get(key);
            if (key.isstring() && value.istable()) {
                BlockNode blockNode = new BlockNode(value.checktable());
                blockNode.setRegistryName(asMcName(key.tojstring()));
                event.getRegistry().register(blockNode);
                blocks.put(key, blockNode);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (LuaValue key : MinetestLib.getRegistry("items").keys()) {
            LuaValue value = MinetestLib.getRegistry("items").get(key);
            if (key.isstring() && value.istable()) {
                if (MinetestLib.getRegistry("nodes").get(key).istable()) {
                    ItemBlockNode itemNode = new ItemBlockNode(blocks.get(key), value.checktable());
                    itemNode.setRegistryName(asMcName(key.tojstring()));
                    event.getRegistry().register(itemNode);
                } else {
                    ItemNode itemNode = new ItemNode(value.checktable());
                    itemNode.setRegistryName(asMcName(key.tojstring()));
                    event.getRegistry().register(itemNode);
                }
            }
        }
    }

    public static File getModDir() {
        return modDir;
    }

    public static ResourceLocation asMcName(String mtName) {
        mtName = MinetestLib.getAliased(mtName);

        if (mtName.startsWith("minecraft_")) {
            // submod
            return new ResourceLocation(mtName.substring(10));
        } else if (mtName.startsWith("minecraft:")) {
            // vanilla
            return new ResourceLocation(mtName);
        } else {
            // minetest
            return new ResourceLocation("minetestbridge", mtName.replace(':', '/'));
        }
    }

    public static String asMtName(ResourceLocation mcName) {
        if (mcName.getResourceDomain().equals("minetestbridge")) {
            // minetest
            return mcName.getResourcePath().replaceFirst("/", ":");
        } else if (mcName.getResourceDomain().equals("minecraft")) {
            // vanilla
            return mcName.toString();
        } else {
            // submod
            return "minecraft_" + mcName.toString();
        }
    }

    public static MinetestMod getMod(String modname) {
        return modMap.get(modname);
    }
}
