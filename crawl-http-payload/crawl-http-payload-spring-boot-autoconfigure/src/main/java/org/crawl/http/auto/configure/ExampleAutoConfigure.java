
package org.crawl.http.auto.configure;

import java.util.ArrayList;
import java.util.List;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.crawl.http.payload.configure.ExampleProperties;
import org.crawl.http.payload.service.ExampleService;
import org.crawl.http.payload.service.impl.ExampleServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

/**
 * @author Roy
 * @date 2020/11/02
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableConfigurationProperties(value = ExampleProperties.class)
public class ExampleAutoConfigure {

    private Logger logger = LoggerFactory.getLogger(ExampleAutoConfigure.class);

    /**
     * 配置ExampleService
     *
     * @return {@link ExampleService}
     */
    @Bean
    @ConditionalOnMissingBean
    public ExampleService exampleService() {
        logger.info("Config ExampleService Start...");
        ExampleServiceImpl service = new ExampleServiceImpl(properties.getId(), properties.getIp());
        logger.info("Config ExampleService End.");
        return service;
    }

    /**
     * 注入ExampleProperties
     */
    private final ExampleProperties properties;

    public ExampleAutoConfigure(ExampleProperties properties) {
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 * * * ? ")
    @SchedulerLock(name = "channelCronName", lockAtMostFor = 5 * 1000, lockAtLeastFor = 5 * 1000)
    public void channelCron() {
        logger.info("*********每小时执行一次");
        try {
            List<String> channels = new ArrayList<>();
            if (CollectionUtils.isEmpty(channels)) {
                System.out.println("");
            }
        }
        catch (Exception e) {
            logger.error("channelCron is error.", e);
        }
    }
}
