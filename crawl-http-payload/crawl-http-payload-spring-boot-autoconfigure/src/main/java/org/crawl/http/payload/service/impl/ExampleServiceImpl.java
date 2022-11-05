package org.crawl.http.payload.service.impl;

import org.crawl.http.payload.configure.ConfigureInfo;
import org.crawl.http.payload.service.ExampleService;

/**
 * 
 * @author Roy
 * @date 2020/11/02
 */
public class ExampleServiceImpl implements ExampleService {

    /**
     * ID
     */
    private String id;
    /**
     * ip
     */
    private String ip;

    /**
     * 构造函数
     *
     * @param id
     *            ID
     * @param ip
     *            IP
     */
    public ExampleServiceImpl(String id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    /**
     * 获取配置信息
     *
     * @return {@link ConfigureInfo}
     */
    @Override
    public ConfigureInfo configInfo() {
        return ConfigureInfo.builder().id(this.id).ip(this.ip).build();
    }

    public String wrap(String word) {
        return id + word + ip;
    }
}
