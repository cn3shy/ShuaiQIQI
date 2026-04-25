package com.shuaiqi.common.constants;

public class Constants {
    public static final Integer HTTP_OK = 200;
    public static final Integer HTTP_UNAUTHORIZED = 401;
    public static final Integer HTTP_FORBIDDEN = 403;
    public static final Integer HTTP_NOT_FOUND = 404;
    public static final Integer HTTP_INTERNAL_ERROR = 500;

    public static final Integer USER_STATUS_ACTIVE = 1;
    public static final Integer USER_STATUS_INACTIVE = 0;

    public static final Integer CONTENT_STATUS_NORMAL = 1;
    public static final Integer CONTENT_STATUS_DELETED = 0;

    public static final Integer COMMENT_STATUS_NORMAL = 1;
    public static final Integer COMMENT_STATUS_DELETED = 0;

    public static final String CACHE_PREFIX_USER = "user:";
    public static final String CACHE_PREFIX_CONTENT = "content:";
    public static final String CACHE_PREFIX_COMMENT = "comment:";

    public static final String REDIS_KEY_USER_TOKEN = "user:token:";
    public static final String REDIS_KEY_USER_INFO = "user:info:";
    public static final String REDIS_KEY_CONTENT_LIKES = "content:likes:";
    public static final String REDIS_KEY_CONTENT_FAVORITES = "content:favorites:";
}
