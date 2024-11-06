package com.heejuk.tuddyfuddy.contextservice.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FormatUtil {

    public static String formatDate(
        LocalDateTime timestamp,
        String pattern
    ) {
        return timestamp.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatTime(
        LocalDateTime timestamp,
        String pattern
    ) {
        return timestamp.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatPreviousDateKST(String pattern) {
        ZonedDateTime previousDayKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                                    .minusDays(1);
        return previousDayKST.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatDateKST(String pattern) {
        ZonedDateTime kstDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 시간에 따라 전날 날짜를 설정
        if (kstDateTime.getHour() < 6) {
            kstDateTime = kstDateTime.minusDays(1);
        }

        return kstDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatTimeKST(String pattern) {
        ZonedDateTime kstDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 만약 새벽 6시 이전이라면 전날 23:00을 기준으로 설정
        if (kstDateTime.getHour() < 6) {
            kstDateTime = kstDateTime.minusDays(1)
                                     .withHour(23)
                                     .withMinute(0)
                                     .withSecond(0)
                                     .withNano(0);
        } else {
            // 현재 시간에서 가장 가까운 정시 단위로 설정
            int hour = kstDateTime.getHour();
            kstDateTime = kstDateTime.withHour(hour)
                                     .withMinute(0)
                                     .withSecond(0)
                                     .withNano(0);
        }

        // 정시 단위에 맞춰 형식화
        return kstDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
