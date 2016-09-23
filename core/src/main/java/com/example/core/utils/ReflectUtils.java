package com.example.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 方法类
 */

public class ReflectUtils {

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object         : 子类对象
     * @param methodName     : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */

    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Method method = null;

        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * 根据方法名称取得反射方法的参数类型(没有考虑同名重载方法使用时注意)
     *
     * @param classInstance 类实例
     * @param methodName    方法名
     * @return
     * @throws ClassNotFoundException
     */
    public static Class[] getMethodParamTypes(Object classInstance,
                                              String methodName)  {
        Class[] paramTypes = null;
        Method[] methods = classInstance.getClass().getMethods();//全部方法
        for (int i = 0; i < methods.length; i++) {
            if (methodName.equals(methods[i].getName())) {//和传入方法名匹配
                Class[] params = methods[i].getParameterTypes();
                paramTypes = new Class[params.length];
                for (int j = 0; j < params.length; j++) {
                    try {
                        paramTypes[j] = Class.forName(params[j].getName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        return paramTypes;
    }

    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected, default)
     *
     * @param object     : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameters : 父类中的方法参数
     * @return 父类中方法的执行结果
     */

    public static Object invokeMethod(Object object, String methodName,
                                      Object[] parameters)  {

        //反射获取接口中方法参数Class文件
        Class[] methodParamTypes = getMethodParamTypes(object, methodName);

        //根据 对象、方法名和对应的方法参数 通过反射 调用上面的方法获取 Method 对象
        Method method = getDeclaredMethod(object, methodName, methodParamTypes);

        if (method == null) {
//            LoggerFactory.getLogger(object.getClass()).saveErrorLog("在类{}中，没有找到方法{}", object, methodName);
        }

        //抑制Java对方法进行检查,主要是针对私有方法而言
        method.setAccessible(true);

        if (null != method) {
            //调用object 的 method 所代表的方法，其方法的参数是 parameters
            try {
                return method.invoke(object, parameters);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object    : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */

    public static Field getDeclaredField(Object object, String fieldName) {
        Field field = null;

        Class<?> clazz = object.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }

        return null;
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object    : 子类对象
     * @param fieldName : 父类中的属性名
     * @param value     : 将要设置的值
     */

    public static void setFieldValue(Object object, String fieldName, Object value) {

        //根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        Field field = getDeclaredField(object, fieldName);

        //抑制Java对其的检查
        field.setAccessible(true);

        try {
            //将 object 中 field 所代表的值 设置为 value
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object    : 子类对象
     * @param fieldName : 父类中的属性名
     * @return : 父类中的属性值
     */

    public static Object getFieldValue(Object object, String fieldName) {

        //根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        Field field = getDeclaredField(object, fieldName);

        //抑制Java对其的检查
        field.setAccessible(true);

        try {
            //获取 object 中 field 所代表的属性值
            return field.get(object);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 父类在构造方法中反射获取子类泛型class文件
     *
     * @param object this
     * @return 子类的泛型数组
     */
    public static Type[] getGenericTypesFromSubclass(Object object) {
        Type genType = object.getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return params;
    }

    public static Class<?> getClazz(String clazzPath) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(clazzPath);
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            LoggerFactory.getLogger("ReflectUtils").error("Cannot find class>>>{}, Exception>>>{}", clazzPath, e);
        }
        return clazz;
    }
}

