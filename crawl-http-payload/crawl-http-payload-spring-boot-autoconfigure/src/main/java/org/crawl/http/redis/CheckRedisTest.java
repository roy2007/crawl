
package org.crawl.http.redis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.crawl.http.vo.User;
import org.crawl.http.vo.Vehicle;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import com.alibaba.fastjson.JSON;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:44:52
 */
@Slf4j
public class CheckRedisTest {

    @Test
    public static void TestPing() {
        Jedis jedis = RedisPoolUtil.getInstance();
        System.out.println(jedis.ping());
    }

    /**
     * 测试java序列化存取
     * 
     * @date 2018年11月27日
     */
    @Test
    public static void TestSerialize() {
        User u = new User(1, "小明", 6);
        RedisOps.setObjectSerialize(User.class.getName().toString() + ":" + 1, u);
        User u2 = (User) RedisOps.getObjectSerialize(User.class.getName().toString() + ":" + 1);
        System.out.println(u2.toString());
    }

    /**
     *  测试json序列化存取
     * 
     * @date 2018年11月27日
     */
    @Test
    public static void TestJson() {
        User u = new User(1, "小明", 6);
        Vehicle v = new Vehicle(1, "渝A00000", "蓝色");
        RedisOps.setObjectJson(User.class.getName().toString() + ":" + 1, u);
        RedisOps.setObjectJson(Vehicle.class.getName().toString() + ":" + 1, v);
        User u2 = (User) RedisOps.<User> getObjectJson(User.class.getName().toString() + ":" + 1, User.class);
        Vehicle v2 = (Vehicle) RedisOps.<Vehicle> getObjectJson(Vehicle.class.getName().toString() + ":" + 1,
                        Vehicle.class);
        System.out.println(u2.toString());
        System.out.println(v2.toString());
    }

    /**
     * 测试hash存取
     * @throws Exception 
     * 
     * @date 2018年11月27日
     */
    @Test
    public static void TestHash() throws Exception {
        Vehicle v = new Vehicle(1, "渝A00000", "蓝色");
        RedisOps.setObjectHash(Vehicle.class.getName().toString() + ":" + 1, v);
        Vehicle v2 = (Vehicle) RedisOps.getObjectHash(Vehicle.class.getName().toString() + ":" + 1, Vehicle.class);
        System.out.println(v2.toString());
    }

    /**
     * 前缀匹配keys
     * @param key
     * @return
     */
    @Test
    public static String[] TestKeysPre(String key) {
        Jedis jedis = RedisPoolUtil.getInstance();
        Set<byte[]> keys = jedis.keys((key + "_*").getBytes());
        Set<String> keystr = new HashSet<>();
        for (byte[] bytes : keys) {
            keystr.add(new String(bytes));
        }
        Object[] objects = keystr.toArray();
        // String[] objecta = (String[]) keystr.toArray();
        String str[] = Arrays.copyOf(objects, objects.length, String[].class);
        RedisPoolUtil.closeConn();
        return str;
    }

    /**
     * 批量查询key的值
     * @param key
     * @return
     */
    @Test
    public static String TestMget(String... key) {
        Jedis jedis = RedisPoolUtil.getInstance();
        List<String> mget = jedis.mget(key);
        RedisPoolUtil.closeConn();
        return mget.toString();
    }

    public static void main(String[] args) {
        Boolean flag = Boolean.FALSE;
        String ROY_PREFIX = "ROY";
        if (flag != null && flag) {
            String[] values = {
                            "{\"boolean\":true,\"string\":\"string\",\"list\":[1,2,3],\"int\":2}",
                            "[\"菜鸟教程\",\"RUNOOB\"]\n",
                            "{'name': 'helloworlda','array':[{'a':'111','b':'222','c':'333'},{'a':'999'}],'address':'111','people':{'name':'happ','sex':'girl'}}",
                            "{\"sex\":\"男\",\"name\":\"张三\",\"age\":25}",
                            "{\"firstName\":\"Brett\",\"lastName\":\"McLaughlin\",\"email\":\"aaaa\"}" };
            for (int i = 0; i < 15; i++) {
                if (i < values.length) {
                    RedisOps.set(ROY_PREFIX + "_" + i, values[i]);
                } else {
                    int number = new Random().nextInt(4) + 1;
                    RedisOps.set(ROY_PREFIX + "_" + i, values[number]);
                }
            }
        }
        TestPing();
        String[] keys = { "ROY_0", "ROY_2", "ROY_4", "ROY_5", "ROY_11" };
        String result = CheckRedisTest.TestMget(keys);
        log.info("mget result:{}", JSON.toJSONString(result));
        String[] valuess = CheckRedisTest.TestKeysPre(ROY_PREFIX);
        log.info("like prefix length: {}, result:{}", valuess.length, JSON.toJSONString(valuess));
    }
}
