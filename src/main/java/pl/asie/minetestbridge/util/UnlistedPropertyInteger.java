package pl.asie.minetestbridge.util;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyInteger implements IUnlistedProperty<Integer> {
    private final String name;
    private final int min, max;

    public UnlistedPropertyInteger(String name) {
        this(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public UnlistedPropertyInteger(String name, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(Integer value) {
        return value != null && value >= min && value <= max;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String valueToString(Integer value) {
        return value.toString();
    }
}
