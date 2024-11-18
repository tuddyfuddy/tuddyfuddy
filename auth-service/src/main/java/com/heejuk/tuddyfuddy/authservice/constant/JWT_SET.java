package com.heejuk.tuddyfuddy.authservice.constant;

public final class JWT_SET {

    public static final Long ACCESS_TOKEN_EXPIRATION = (long) (1000 * 60 * 60 * 24); // 엑세스 토큰 유효시간 : 24시간
    public static final Long REFRESH_TOKEN_EXPIRATION = (long) (1000 * 60 * 60 * 72); // 리프레시 토큰 유효시간 : 72시간

    private JWT_SET() {
    }
}