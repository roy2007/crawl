/*package org.crawl.http.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
*//**
 *
 * @author Roy
 *
 * @date 2021年3月20日-下午10:13:01
 *//*

   

    @Configuration
    public class RedisConfig {
        @Value("${redis.host}")
        private String redisHost;

        @Value("${redis.port}")
        private int redisPort;

        @Value("${redis.password}")
        private String password;

        @Value("${redis.taskScheduler.poolSize}")
        private int tasksPoolSize;
        @Value("${redis.taskScheduler.defaultLockMaxDurationMinutes}")
        private int lockMaxDuration;

        @Bean(destroyMethod = "shutdown")
        ClientResources clientResources() {
            return DefaultClientResources.create();
        }

        @Bean
        public RedisStandaloneConfiguration redisStandaloneConfiguration() {
            RedisStandaloneConfiguration redisStandaloneConfiguration =
                    new RedisStandaloneConfiguration(redisHost, redisPort);
            if (password != null && !password.trim().equals("")) {
                RedisPassword redisPassword = RedisPassword.of(password);
                redisStandaloneConfiguration.setPassword(redisPassword);
            }
            return redisStandaloneConfiguration;
        }

        @Bean
        public ClientOptions clientOptions() {
            return ClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .autoReconnect(true).build();
        }

        @Bean
        LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options, ClientResources dcr) {
            return LettucePoolingClientConfiguration.builder().poolConfig(new GenericObjectPoolConfig())
                    .clientOptions(options).clientResources(dcr).build();
        }

        @Bean
        public RedisConnectionFactory connectionFactory(
                RedisStandaloneConfiguration redisStandaloneConfiguration,
                LettucePoolingClientConfiguration lettucePoolConfig) {
            return new LettuceConnectionFactory(redisStandaloneConfiguration, lettucePoolConfig);
        }

        @Bean
        @ConditionalOnMissingBean(name = "redisTemplate")
        @Primary
        public RedisTemplate<Object, Object> redisTemplate(
                RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<Object, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }

        @Bean
        public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
            return new RedisLockProvider(connectionFactory);
        }

        @Bean
        public ScheduledLockConfiguration taskSchedulerLocker(LockProvider lockProvider) {
            return ScheduledLockConfigurationBuilder.withLockProvider(lockProvider)
                    .withPoolSize(tasksPoolSize).withDefaultLockAtMostFor(Duration.ofMinutes(lockMaxDuration))
                    .build();
        }
}
*/