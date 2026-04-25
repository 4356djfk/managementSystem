package com.manage.managesystem.controller;

import com.manage.managesystem.auth.AuthInterceptor;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.LoginDto;
import com.manage.managesystem.dto.RegisterDto;
import com.manage.managesystem.service.AuthService;
import com.manage.managesystem.vo.LoginVO;
import com.manage.managesystem.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthInterceptor authInterceptor;

    public AuthController(AuthService authService, AuthInterceptor authInterceptor) {
        this.authService = authService;
        this.authInterceptor = authInterceptor;
    }

    @PostMapping("/register")
    public ApiResponse<UserProfileVO> register(@Valid @RequestBody RegisterDto dto) {
        return ApiResponse.success(authService.register(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDto dto) {
        return ApiResponse.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(authInterceptor.resolveToken(request));
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
