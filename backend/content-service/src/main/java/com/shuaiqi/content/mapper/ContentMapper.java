package com.shuaiqi.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuaiqi.content.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 内容Mapper
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 原子递增点赞数
     */
    @Update("UPDATE content SET like_count = like_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementLikeCount(@Param("contentId") Long contentId);

    /**
     * 原子递减点赞数
     */
    @Update("UPDATE content SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{contentId} AND status = 1")
    int decrementLikeCount(@Param("contentId") Long contentId);

    /**
     * 原子递增收藏数
     */
    @Update("UPDATE content SET favorite_count = favorite_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementFavoriteCount(@Param("contentId") Long contentId);

    /**
     * 原子递减收藏数
     */
    @Update("UPDATE content SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{contentId} AND status = 1")
    int decrementFavoriteCount(@Param("contentId") Long contentId);

    /**
     * 原子递增评论数
     */
    @Update("UPDATE content SET comment_count = GREATEST(comment_count + #{increment}, 0), update_time = NOW() WHERE id = #{contentId} AND status = 1")
    int updateCommentCount(@Param("contentId") Long contentId, @Param("increment") Integer increment);

    /**
     * 原子递增浏览量
     */
    @Update("UPDATE content SET view_count = view_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementViewCount(@Param("contentId") Long contentId);
}
