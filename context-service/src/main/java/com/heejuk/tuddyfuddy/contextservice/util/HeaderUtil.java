package com.heejuk.tuddyfuddy.contextservice.util;

import java.text.NumberFormat;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@Slf4j
public class HeaderUtil {

    public static String getUserHeaderInfo(HttpHeaders headers) {
        String userId = null;
        if (headers.get("X-UserId") != null) {
            userId = headers.get("X-UserId")
                            .get(0);
        }
        log.info("userId = {}", userId);
        return userId;
    }

    public static String formatWithCommas(long amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        return numberFormat.format(amount);
    }
}
