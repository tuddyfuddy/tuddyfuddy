package com.heejuk.tuddyfuddy.contextservice.util;

import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ParseUtil {

    private final ApplicationContext applicationContext;

    /**
     * 주어진 경로의 CSV 파일을 파싱합니다.
     *
     * @param resourcePath 리소스 경로
     * @param skipLines    스킵할 행 수
     * @return 파싱된 CSV 데이터 리스트
     */
    public List<String[]> parseCsv(
        String resourcePath,
        int skipLines
    ) {
        try {
            Resource resource = applicationContext.getResource(resourcePath);
            try (CSVReader reader = new CSVReader(
                new InputStreamReader(resource.getInputStream(), "euc-kr"))) {

                // Skip the specified number of rows
                for (int i = 0; i < skipLines; i++) {
                    reader.readNext();
                }

                return reader.readAll();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file at path: " + resourcePath, e);
        }
    }
}
