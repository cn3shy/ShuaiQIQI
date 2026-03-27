package com.shuaiqi.common.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {

    /**
     * 普通用户
     */
    USER("user", "普通用户"),

    /**
     * 管理员
     */
    ADMIN("admin", "管理员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return USER;
    }
}
