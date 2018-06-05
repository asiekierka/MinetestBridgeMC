package pl.asie.minetestbridge;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

public class MinetestSettingsLib extends LuaTable {
    public MinetestSettingsLib() {
        LuaProxy.reflect(this, this);
    }

    @LuaMethod
    public LuaValue get(LuaValue wrap, String name) {
        return LuaValue.NIL;
    }

    @LuaMethod
    public LuaValue get_bool(LuaValue wrap, String name) {
        return LuaValue.valueOf(false);
    }
}
