package com.cityrepair.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 维修人员工单详情VO
 */
public class WorkerOrderDetailVO {

    /**
     * 工单ID
     */
    private Long id;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 报修类别ID
     */
    private Long categoryId;

    /**
     * 报修类别名称
     */
    private String categoryName;

    /**
     * 故障地点
     */
    private String location;

    /**
     * 故障描述
     */
    private String description;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 状态
     */
    private String status;

    /**
     * 居民姓名
     */
    private String residentName;

    /**
     * 居民联系电话
     */
    private String residentPhone;

    /**
     * 完成结果描述
     */
    private String completionResult;

    /**
     * 派单时间
     */
    private LocalDateTime assignedAt;

    /**
     * 接单时间
     */
    private LocalDateTime acceptedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 附件列表
     */
    private List<OrderAttachmentVO> attachments;

    /**
     * 状态日志列表
     */
    private List<OrderStatusLogVO> statusLogs;

    // 构造方法
    public WorkerOrderDetailVO() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getResidentPhone() {
        return residentPhone;
    }

    public void setResidentPhone(String residentPhone) {
        this.residentPhone = residentPhone;
    }

    public String getCompletionResult() {
        return completionResult;
    }

    public void setCompletionResult(String completionResult) {
        this.completionResult = completionResult;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderAttachmentVO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<OrderAttachmentVO> attachments) {
        this.attachments = attachments;
    }

    public List<OrderStatusLogVO> getStatusLogs() {
        return statusLogs;
    }

    public void setStatusLogs(List<OrderStatusLogVO> statusLogs) {
        this.statusLogs = statusLogs;
    }

    /**
     * 附件VO内部类
     */
    public static class OrderAttachmentVO {
        private Long id;
        private String attachmentType;
        private String fileUrl;
        private String originalName;
        private String contentType;
        private Long fileSize;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getAttachmentType() { return attachmentType; }
        public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 状态日志VO内部类
     */
    public static class OrderStatusLogVO {
        private Long id;
        private Long operatorId;
        private String operatorName;
        private String action;
        private String fromStatus;
        private String toStatus;
        private String remark;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getFromStatus() { return fromStatus; }
        public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
        public String getToStatus() { return toStatus; }
        public void setToStatus(String toStatus) { this.toStatus = toStatus; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
