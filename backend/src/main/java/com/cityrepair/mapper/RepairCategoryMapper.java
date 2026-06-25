package com.cityrepair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityrepair.entity.RepairCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报修类别Mapper接口
 */
@Mapper
public interface RepairCategoryMapper extends BaseMapper<RepairCategory> {
}
