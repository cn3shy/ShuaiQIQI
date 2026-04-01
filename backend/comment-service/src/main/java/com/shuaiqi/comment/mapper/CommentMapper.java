package com.shuaiqi.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuaiqi.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 原子递增点赞数
     */
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId} AND status = 1")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 原子递减点赞数
     */
    @Update("UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId} AND status = 1")
    int decrementLikeCount(@Param("commentId") Long commentId);
}
