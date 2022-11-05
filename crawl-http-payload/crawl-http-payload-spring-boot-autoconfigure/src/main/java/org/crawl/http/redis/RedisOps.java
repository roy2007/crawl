
package org.crawl.http.redis;

import redis.clients.jedis.Jedis;
import com.alibaba.fastjson.JSON;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:40:26
 */
public class RedisOps {

    /**
     * kv字符串存
     * @param key
     * @param value 
     * @author corleone
     * @date 2018年11月15日
     */
    public static void set(String key, String value) {
        Jedis jedis = RedisPoolUtil.getInstance();
        jedis.set(key, value);
        RedisPoolUtil.closeConn();
    }

    /**
     * kv字符串取
     * @param key
     * @return 字符串
     * @author corleone
     * @date 2018年11月15日
     */
    public static String get(String key) {
        Jedis jedis = RedisPoolUtil.getInstance();
        String value = jedis.get(key);
        RedisPoolUtil.closeConn();
        return value;
    }

    /**
     * kv对象存(java序列化方式)
     * @param key
     * @param object 对象类必须实现序列化
     * @author corleone
     * @date 2018年11月15日
     */
    public static void setObjectSerialize(String key, Object object) {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return;
        }
        jedis.set(key.getBytes(), SerializeUtil.serizlize(object));
        RedisPoolUtil.closeConn();
    }

    /**
     * kv对象取(java反序列化)
     * @param key
     * @return 对象
     * @author corleone
     * @date 2018年11月15日
     */
    public static Object getObjectSerialize(String key) {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return null;
        }
        byte[] bytes = jedis.get(key.getBytes());
        RedisPoolUtil.closeConn();
        if (bytes == null) {
            return null;
        }
        return SerializeUtil.deserialize(bytes);
    }

    /**
     * 删除key
     * @param key 
     * @author corleone
     * @date 2018年11月15日
     */
    public static void del(String key) {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return;
        }
        jedis.del(key.getBytes());
        RedisPoolUtil.closeConn();
    }

    /**
     * kv对象存(json方式)
     * @param key
     * @param object 
     * @author corleone
     * @date 2018年11月15日
     */
    public static void setObjectJson(String key, Object object) {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return;
        }
        jedis.set(key, JSON.toJSONString(object));
        RedisPoolUtil.closeConn();
    }

    /**
     * kv对象取(json方式)
     * @param key
     * @param clazz反序列化对象类型
     * @return 对象
     * @author corleone
     * @date 2018年11月15日
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Object getObjectJson(String key, Class<?> clazz) {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return null;
        }
        String result = jedis.get(key);
        RedisPoolUtil.closeConn();
        if (result == null) {
            return null;
        }
        T obj = (T) JSON.parseObject(result, clazz);
        return obj;
    }

    /**
     * kv对象存(map形势)
     * @param key
     * @param u
     * @throws Exception 
     * @author corleone
     * @date 2018年11月27日
     */
    public static void setObjectHash(String key, Object u) throws Exception {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return;
        }
        jedis.hmset(key, MapUtil.objectToMap(u));
        RedisPoolUtil.closeConn();
    }

    /**
     * kv对象取(map形势)
     * @param key
     * @param clazz
     * @throws Exception 
     * @author corleone
     * @date 2018年11月27日
     */
    public static Object getObjectHash(String key, Class<?> clazz) throws Exception {
        Jedis jedis = RedisPoolUtil.getInstance();
        if (jedis == null) {
            return null;
        }
        Object obj = MapUtil.mapToObject(jedis.hgetAll(key), clazz);
        RedisPoolUtil.closeConn();
        if (obj == null) {
            return null;
        }
        return obj;
    }
}
