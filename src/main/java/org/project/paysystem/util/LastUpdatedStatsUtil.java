package org.project.paysystem.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LastUpdatedStatsUtil {

    private final RestTemplate restTemplate;

    @Value("${url.base}")
    private String baseUrl;

    public LocalDate fetchLastUpdatedStatsDate(String period, String revenueType) {
        String part = period;
        if(revenueType != null || revenueType != "") {
            part = part + "/" + revenueType;
        }

        String url = baseUrl + "/api/stats/"+part+"/last-updated";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String responseBody = response.getBody().replace("\"", "");
            return LocalDate.parse(responseBody);
        } else {
            throw new RuntimeException("Failed to fetch last updated "+period+" stats date.");
        }
    }
}
