package com.cityrepair.dto;

/**
 * 完成工单请求DTO
 */
public class CompleteRequest {

    /**
     * 完成结果描述（必填）
     */
    private String completionResult;

    // 构造方法
    public CompleteRequest() {}

    // Getter和Setter方法
    public String getCompletionResult() {
        return completionResult;
    }

    public void setCompletionResult(String completionResult) {
        this.completionResult = completionResult;
    }
}
