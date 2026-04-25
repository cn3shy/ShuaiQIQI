package com.shuaiqi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuaiqi.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    @Update("UPDATE content SET like_count = like_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementLikeCount(@Param("contentId") Long contentId);

    @Update("UPDATE content SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{contentId} AND status = 1")
    int decrementLikeCount(@Param("contentId") Long contentId);

    @Update("UPDATE content SET favorite_count = favorite_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementFavoriteCount(@Param("contentId") Long contentId);

    @Update("UPDATE content SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{contentId} AND status = 1")
    int decrementFavoriteCount(@Param("contentId") Long contentId);

    @Update("UPDATE content SET comment_count = GREATEST(comment_count + #{increment}, 0), update_time = NOW() WHERE id = #{contentId} AND status = 1")
    int updateCommentCount(@Param("contentId") Long contentId, @Param("increment") Integer increment);

    @Update("UPDATE content SET view_count = view_count + 1 WHERE id = #{contentId} AND status = 1")
    int incrementViewCount(@Param("contentId") Long contentId);

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM content WHERE status = 1")
    Long sumLikeCount();
}
