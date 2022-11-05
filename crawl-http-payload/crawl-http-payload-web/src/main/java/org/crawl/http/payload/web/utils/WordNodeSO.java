package org.crawl.http.payload.web.utils;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  
 * @author Roy
 * @date 2020/12/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordNodeSO implements Serializable {
    private static final long serialVersionUID = -4220382279217948668L;
    /**
     * 标题
     */
    private String title;

    /**
     * 无格式的标题
     */
    private String pureTitle;

    /**
     * 保留格式内容
     */
    private String content;

    /**
     * 保留格式内容，头节点之前所有word内容部份
     */
    private String previousContent;

    /**
     * 对应文档中标题级别
     */
    private Integer level;

    /**
     * 是否页子
     */
    private Boolean isLeaf;

    /**
     * 子节点
     */
    private List<WordNodeSO> childrens;

    /**
     * 行偏移位置
     */
    private Integer rowOffsetNumber;

    /**
     * 标题对应正文结束位置
     */
    private Integer endNumber;

    /**
     * 包括表格
     */
    private Boolean includeTable;

}
