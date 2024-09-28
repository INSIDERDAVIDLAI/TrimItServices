// src/main/java/com/example/demo/config/CustomOAuth2SuccessHandler.java
package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getAttribute("name"); // or "email" or any other attribute

        // Check if user exists, if not, register the user
        if (!userService.userExists(username)) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(""); // Set a default or random password, or handle it differently
            userService.registerUser(newUser);
        }

        // Generate token
        String token = userService.generateTokenForOAuth2User(oAuth2User);

        // Redirect to frontend with token
        response.sendRedirect("http://localhost:5173/login?token=" + token);
    }
}