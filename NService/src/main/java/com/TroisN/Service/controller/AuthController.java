package com.TroisN.Service.controller;

import com.TroisN.Service.dto.auth.GoogleLoginRequest;
import com.TroisN.Service.dto.auth.TokenResponse;
import com.TroisN.Service.dto.user.LoginRequest;
import com.TroisN.Service.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;


    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return loginService.login(request);
    }


    @GetMapping("/whoami")
    public Collection<? extends GrantedAuthority> whoami(
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return List.of();
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess != null
                ? (List<String>) realmAccess.getOrDefault("roles", List.of())
                : List.of();

        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();
    }


    @PostMapping("/refresh")
    public TokenResponse refresh(
            @RequestParam("refresh_token") String refreshToken
    ) {
        return loginService.refreshToken(refreshToken);
    }


    @PostMapping("/complete-google-login")
    public ResponseEntity<Void> completeGoogleLogin(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        loginService.completeGoogleLogin(jwt, request.getRole());
        return ResponseEntity.ok().build();
    }
}
