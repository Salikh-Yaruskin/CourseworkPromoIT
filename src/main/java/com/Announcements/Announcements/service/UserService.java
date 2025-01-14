package com.Announcements.Announcements.service;

import com.Announcements.Announcements.dto.LoginDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public String verify(LoginDTO loginDTO) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password()));
        if (authentication.isAuthenticated()){
            log.info("Пользователь {} успешно авторизован.", loginDTO.username());
            return jwtService.generateToken(loginDTO.username());
        }

        log.warn("Не удалось авторизовать пользователя: {}", loginDTO.username());
        return "Fail";
    }
}
