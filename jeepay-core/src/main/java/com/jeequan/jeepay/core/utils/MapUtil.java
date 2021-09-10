package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author axl rose
 * @date 2021/9/9
 */
public class MapUtil {

    /**
     *
     * @param map
     * @param beanClass
     * @return
     * @throws Exception
     */
    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null) {
            return null;
        }
        Object object = beanClass.newInstance();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(object, map.get(field.getName()));
        }
        return object;
    }

    /**
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static Map<String, Object> objectToMap(Object obj)  {
        try {
            if (obj == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
            return map;
        }catch (Exception e){
            return new HashMap<>(0);
        }
    }

    public static TreeMap<String,String> convertToMap(String jsonParam){
        TreeMap<String,String> treeMap = new TreeMap<>();
        JSONObject jsonObject = JSONObject.parseObject(jsonParam);
        for(String key :jsonObject.keySet()){
            treeMap.put(key,jsonObject.getString(key));
        }
        return treeMap;
    }

}
