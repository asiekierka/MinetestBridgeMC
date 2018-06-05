package pl.asie.minetestbridge.util;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class MethodHandleHelper {
    private MethodHandleHelper() {

    }

    public static Method reflectMethodRecurse(Class<?> c, String deobfName, String obfName, Class... parameterTypes) {
        String nameToFind = ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) ? deobfName : obfName;

        while (c != null) {
            try {
                Method m = c.getDeclaredMethod(nameToFind, parameterTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                Class<?> sc = c.getSuperclass();
                c = (sc == c) ? null : sc;
            } catch (Exception e) {
                throw new ReflectionHelper.UnableToFindMethodException(e);
            }
        }

        throw new ReflectionHelper.UnableToFindMethodException(new Throwable());
    }

    public static MethodHandle findConstructor(String s, Class<?>... types) {
        try {
            return MethodHandles.lookup().unreflectConstructor(
                    ReflectionHelper.findConstructor(Class.forName(s), types)
            );
        } catch (IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findMethod(Class c, String nameDeobf, String nameObf, Class<?>... types) {
        try {
            return MethodHandles.lookup().unreflect(
                    ReflectionHelper.findMethod(c, nameDeobf, nameObf, types)
            );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldGetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectSetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldGetter(String s, String... names) {
        try {
            return findFieldGetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(String s, String... names) {
        try {
            return findFieldSetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
