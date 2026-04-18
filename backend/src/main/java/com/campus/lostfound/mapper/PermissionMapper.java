package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}