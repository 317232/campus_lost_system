package com.campus.lostfound.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.domain.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
