package com.cityrepair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityrepair.entity.OrderAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 工单附件Service接口
 */
public interface OrderAttachmentService extends IService<OrderAttachment> {

    /**
     * 上传附件
     * @param orderId 工单ID
     * @param file 文件
     * @param attachmentType 附件类型
     * @param uploadedBy 上传者ID
     * @return 保存后的附件对象
     */
    OrderAttachment uploadAttachment(Long orderId, MultipartFile file,
                                     String attachmentType, Long uploadedBy);

    /**
     * 查询工单的附件列表
     * @param orderId 工单ID
     * @param attachmentType 附件类型（可选）
     * @return 附件列表
     */
    List<OrderAttachment> listByOrderId(Long orderId, String attachmentType);
}
