package com.alice.learn.common;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long requireUserId() {
        Long userId = CurrentUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return userId;
    }
}
