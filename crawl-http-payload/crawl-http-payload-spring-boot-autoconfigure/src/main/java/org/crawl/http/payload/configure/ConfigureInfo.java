package org.crawl.http.payload.configure;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * @author Roy
 * @date 2020/11/02
 */
@Data
@Builder
public class ConfigureInfo implements Serializable {

    private static final long serialVersionUID = -5094070929298019210L;
    /**
     * ID
     */
    private String id;
    /**
     * IP地址
     */
    private String ip;
    private String            others;
}
