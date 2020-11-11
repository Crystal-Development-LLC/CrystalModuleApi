package club.cannoner.crystalmoduleapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionHelper {
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?> ...params) {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }

        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (params.length > 0) {
                    if (Arrays.equals(method.getParameterTypes(), params)) {
                        method.setAccessible(true);
                        return method;
                    }
                } else {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }
}
