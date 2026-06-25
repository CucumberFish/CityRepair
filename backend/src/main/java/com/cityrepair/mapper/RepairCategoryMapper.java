package com.cityrepair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityrepair.entity.RepairCategory;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RepairCategoryMapper extends BaseMapper<RepairCategory> {
    @Select("SELECT * FROM repair_category WHERE enabled = 1 ORDER BY sort_order ASC")
    List<RepairCategory> findEnabled();
}
