package com.shuaiqi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuaiqi.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId} AND status = 1")
    int incrementLikeCount(@Param("commentId") Long commentId);

    @Update("UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId} AND status = 1")
    int decrementLikeCount(@Param("commentId") Long commentId);
}
