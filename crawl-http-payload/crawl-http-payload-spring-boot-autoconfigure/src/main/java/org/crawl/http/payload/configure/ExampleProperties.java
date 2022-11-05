package org.crawl.http.payload.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.crawl.http.payload.configure.ExampleProperties.DEFAULT_PREFIX;

/**
 * @author Roy
 * @date 2020/11/02
 */
@Data
@ConfigurationProperties(value = DEFAULT_PREFIX)
public class ExampleProperties {
    /**
     * PREFIX
     */
    public static final String DEFAULT_PREFIX = "crawl.http.payload.example";
    /**
     * ID标识
     */
    private String id;

    /**
     * IP地址
     */
    private String ip;

}