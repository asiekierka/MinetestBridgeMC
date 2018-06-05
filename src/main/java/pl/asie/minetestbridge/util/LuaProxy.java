package pl.asie.minetestbridge.util;

import jdk.nashorn.internal.lookup.MethodHandleFactory;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.lib.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class LuaProxy {
    public static LuaValue reflect(Object object) {
        return reflect(LuaValue.tableOf(), object);
    }

    public static LuaValue reflect(LuaValue table, Object object) {
        for (Field f : object.getClass().getFields()) {
            try {
                LuaField lfAnn = f.getAnnotation(LuaField.class);
                if (lfAnn != null) {
                    Object o = f.get(object);
                    if (o instanceof LuaValue) {
                        table.set(f.getName(), (LuaValue) o);
                    } else {
                        throw new RuntimeException("Could not translate field " + f.getType() + "!");
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        for (Method m : object.getClass().getMethods()) {
            try {
                LuaMethod lmAnn = m.getAnnotation(LuaMethod.class);
                if (lmAnn != null) {
                    MethodHandle handle = MethodHandles.lookup().unreflect(m);
                    table.set(m.getName(), new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            Class<?>[] parameterClasses = m.getParameterTypes();
                            Type[] parameterTypes = m.getGenericParameterTypes();
                            Object[] invokeArgs = new Object[m.getParameterCount() + 1];
                            invokeArgs[0] = object; // owner

                            for (int i = 0; i < m.getParameterCount(); i++) {
                                Class<?> cl = parameterClasses[i];
                                Type type = parameterTypes[i];
                                if (cl == LuaValue.class) {
                                    invokeArgs[i + 1] = args.arg(i + 1);
                                } else if (cl == String.class) {
                                    try {
                                        invokeArgs[i + 1] = args.checkjstring(i + 1);
                                    } catch (LuaError e) {
                                        invokeArgs[i + 1] = null;
                                    }
                                } else {
                                    throw new RuntimeException("Could not translate method parameter " + type.getClass() + "!");
                                }
                            }

                            try {
                                Object o = handle.invokeWithArguments(invokeArgs);
                                if (o instanceof Varargs) {
                                    return (Varargs) o;
                                } else if (o instanceof LuaValue[])  {
                                    return LuaValue.varargsOf((LuaValue[]) o);
                                } else if (o == null) {
                                    return LuaValue.NIL;
                                } else {
                                    throw new RuntimeException("Could not translate return value " + o.getClass() + "!");
                                }
                            } catch (Throwable t) {
                                throw new RuntimeException(t);
                            }
                        }
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return table;
    }
}
