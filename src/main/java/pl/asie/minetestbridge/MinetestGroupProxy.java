package pl.asie.minetestbridge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.luaj.vm2.ast.Str;

public class MinetestGroupProxy {
    private static final BiMap<String, String> minetestToMinecraft;

    static {
        minetestToMinecraft = HashBiMap.create();
        minetestToMinecraft.put("stick", "stickWood");
    }

    public static String toMinecraft(String s) {
        return minetestToMinecraft.getOrDefault(s, s);
    }

    public static String toMinetest(String s) {
        return minetestToMinecraft.inverse().getOrDefault(s, s);
    }
}
