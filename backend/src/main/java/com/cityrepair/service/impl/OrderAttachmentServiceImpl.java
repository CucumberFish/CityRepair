package com.cityrepair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityrepair.entity.OrderAttachment;
import com.cityrepair.exception.BusinessException;
import com.cityrepair.mapper.OrderAttachmentMapper;
import com.cityrepair.service.OrderAttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 工单附件Service实现类
 */
@Service
public class OrderAttachmentServiceImpl extends ServiceImpl<OrderAttachmentMapper, OrderAttachment>
        implements OrderAttachmentService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private Long maxFileSize;

    @Override
    public OrderAttachment uploadAttachment(Long orderId, MultipartFile file,
                                            String attachmentType, Long uploadedBy) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new BusinessException("只支持图片文件上传");
        }

        // 生成唯一的文件名
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString() + extension;
        String relativePath = datePath + "/" + fileName;

        // 解析绝对路径（如果是相对路径，基于项目根目录）
        String absoluteUploadPath = uploadPath;
        if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
            // 相对路径，转换为绝对路径
            absoluteUploadPath = System.getProperty("user.dir") + uploadPath.substring(1);
        }

        // 创建上传目录
        File dir = new File(absoluteUploadPath + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            File dest = new File(absoluteUploadPath + "/" + relativePath);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }

        // 保存附件记录
        OrderAttachment attachment = new OrderAttachment();
        attachment.setOrderId(orderId);
        attachment.setAttachmentType(attachmentType);
        attachment.setFileUrl(relativePath);
        attachment.setOriginalName(originalName);
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(uploadedBy);

        save(attachment);
        return attachment;
    }

    @Override
    public List<OrderAttachment> listByOrderId(Long orderId, String attachmentType) {
        LambdaQueryWrapper<OrderAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderAttachment::getOrderId, orderId);
        if (attachmentType != null && !attachmentType.isEmpty()) {
            wrapper.eq(OrderAttachment::getAttachmentType, attachmentType);
        }
        wrapper.orderByDesc(OrderAttachment::getCreatedAt);
        return list(wrapper);
    }
}
