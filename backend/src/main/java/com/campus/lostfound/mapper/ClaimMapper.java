package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.domain.entity.Claim;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClaimMapper extends BaseMapper<Claim> {
}
