package pl.asie.minetestbridge.backwards;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.util.LuaMethod;
import pl.asie.minetestbridge.util.LuaProxy;

public class EntityWrapped extends LuaTable {
    private final Entity entity;

    public EntityWrapped(Entity entity) {
        this.entity = entity;
        LuaProxy.reflect(this, this);
    }

    @LuaMethod
    public LuaValue is_player(LuaValue wrap) {
        return LuaValue.valueOf(entity instanceof EntityPlayer);
    }

    @LuaMethod
    public LuaValue get_player_name(LuaValue wrap) {
        return LuaValue.valueOf(entity.getName());
    }
}
