
package org.crawl.http.redis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:38:47
 */
public class MapUtil {

    /**
     * map转对象(目前只支持对象中含有String和int类型的属性,其他类型需要自己扩展)
     * @param map
     * @param beanClass
     * @throws Exception 
     * @author corleone
     * @date 2018年11月27日
     */
    public static <T> Object mapToObject(Map<String, String> map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;
        Object obj = beanClass.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            if (field.getType().equals(int.class)) {
                field.set(obj, Integer.parseInt(map.get(field.getName())));
            } else {
                field.set(obj, (Object) map.get(field.getName()));
            }
        }
        return obj;
    }

    /**
     * 对象转map(目前只支持对象属性为基本类型)
     * @param obj
     * @throws Exception 
     * @author corleone
     * @date 2018年11月27日
     */
    public static Map<String, String> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj).toString());
        }
        return map;
    }
}
