package com.Announcements.Announcements.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {

    @Value("${recaptcha.secret.key}")
    private String recaptchaSecretKey;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    // Добавляем специальный токен для тестового варианта
    private static final String TEST_CAPTCHA_TOKEN = "test-captcha-token";

    public boolean validateCaptcha(String captchaResponse) {
        if (TEST_CAPTCHA_TOKEN.equals(captchaResponse)) {
            return true;
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.postForObject(
                RECAPTCHA_VERIFY_URL + "?secret=" + recaptchaSecretKey + "&response=" + captchaResponse,
                null, Map.class
        );
        return (Boolean) response.get("success");
    }
}
