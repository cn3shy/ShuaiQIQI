package com.shuaiqi.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuaiqi.user.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
