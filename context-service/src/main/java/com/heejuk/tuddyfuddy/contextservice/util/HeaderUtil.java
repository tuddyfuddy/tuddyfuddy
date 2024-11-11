package com.heejuk.tuddyfuddy.contextservice.util;

import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.http.HttpHeaders;

public class HeaderUtil {

    public static String getUserHeaderInfo(HttpHeaders headers) {

        return headers.get("X-UserId")
                      .get(0);
    }

    public static String formatWithCommas(long amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        return numberFormat.format(amount);
    }
}
