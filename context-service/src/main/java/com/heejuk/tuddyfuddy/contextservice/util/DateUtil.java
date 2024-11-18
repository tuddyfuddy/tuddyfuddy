package com.heejuk.tuddyfuddy.contextservice.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ZONE_KST);
    }
}