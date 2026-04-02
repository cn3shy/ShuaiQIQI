package com.shuaiqi.common.utils;

import com.shuaiqi.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static Long getUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            throw BusinessException.unauthorized("请先登录");
        }
        return Long.parseLong(userId);
    }

    public static Long getUserIdFromRequestOrNull(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
