package com.cityrepair.dto;

/**
 * 提交进度请求DTO
 */
public class ProgressRequest {

    /**
     * 进度说明内容
     */
    private String content;

    // 构造方法
    public ProgressRequest() {}

    // Getter和Setter方法
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
