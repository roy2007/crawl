package org.crawl.http.redis;

import java.util.Properties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:35:19
 */
public class RedisPoolUtil {

    private volatile static JedisPool jedisPool = null;
    private static String redisConfigFile = "redis.properties";
    // 把redis连接对象放到本地线程中
    private static ThreadLocal<Jedis> local = new ThreadLocal<Jedis>();

    private RedisPoolUtil() {
    }

    /**
     * 初始化连接池
     * @author corleone
     * @date 2018年11月27日
     */
    public static void initialPool() {
        try {
            Properties props = new Properties();
            // 加载连接池配置文件
            props.load(RedisPoolUtil.class.getClassLoader().getResourceAsStream(redisConfigFile));
            // 创建jedis池配置实例
            JedisPoolConfig config = new JedisPoolConfig();
            // 设置池配置项值
            config.setMaxTotal(Integer.valueOf(props.getProperty("pool.maxTotal")));
            config.setMaxIdle(Integer.valueOf(props.getProperty("pool.maxIdle")));
            config.setMaxWaitMillis(Long.valueOf(props.getProperty("pool.maxWaitMillis")));
            // 根据配置实例化jedis池
            jedisPool = new JedisPool(config, props.getProperty("redis.ip"), Integer.valueOf(props
                            .getProperty("redis.port")), Integer.valueOf(props.getProperty("redis.timeout")),
                            "".equals(props.getProperty("redis.passWord")) ? null : props.getProperty("redis.passWord"));
            System.out.println("连接池初始化成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("连接池初始化失败");
        }
    }

    /**
     * 获取连接
     * @return 
     * @author corleone
     * @date 2018年11月27日
     */
    public static Jedis getInstance() {
        // Redis对象
        Jedis jedis = local.get();
        if (jedis == null) {
            if (jedisPool == null) {
                synchronized (RedisPoolUtil.class) {
                    if (jedisPool == null) {
                        initialPool();
                    }
                }
            }
            try {
                jedis = jedisPool.getResource();
            }
            catch (JedisConnectionException e) {
                e.printStackTrace();
            }
            local.set(jedis);
        }
        return jedis;
    }

    /**
     * 关闭连接
     * @author corleone
     * @date 2018年11月27日
     */
    public static void closeConn() {
        Jedis jedis = local.get();
        if (jedis != null) {
            jedis.close();
        }
        local.set(null);
    }
}
