package org.crawl.http.redis;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.alibaba.fastjson.JSON;

/**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午9:34:23
 */
@Slf4j
public class RedisPoolPrimary {
    public static final String ENV_ID = "MyAppId";

    private JedisPool pool;
    public RedisPoolPrimary(@Value("${spring.redis.url}") String redisCloudUrl,
                    @Value("${meetical.redis.jedis.JEDIS_POOL_MAX_TOTAL}") int jedisPoolMaxTotal,
                    @Value("${meetical.redis.jedis.JEDIS_POOL_MAX}") int jedisPoolMax,
                    @Value("${meetical.redis.jedis.JEDIS_POOLMAX_IDLE}") int jedisPoolMaxIdle) {
        URI redisUrl = URI.create(redisCloudUrl);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(jedisPoolMaxTotal);
        poolConfig.setMinIdle(jedisPoolMax);
        poolConfig.setMaxIdle(jedisPoolMaxIdle);
        log.info("the poolConfig :{}", JSON.toJSONString(poolConfig));
        pool = new JedisPool(poolConfig, redisUrl);
    }
    public JedisPool getPool() {
        return pool;
    }
}
