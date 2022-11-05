package org.crawl.http.payload.service;

import org.crawl.http.payload.configure.ConfigureInfo;

/**
 * @author Roy
 * @date 2020/11/02
 */
public interface ExampleService {
    /**
     * 获取配置信息
     *
     * @return {@link ConfigureInfo}
     */
    public ConfigureInfo configInfo();

    /**
     * 包装方法测试
     * 
     * @param word
     * @return
     */
    public String wrap(String word);
}
