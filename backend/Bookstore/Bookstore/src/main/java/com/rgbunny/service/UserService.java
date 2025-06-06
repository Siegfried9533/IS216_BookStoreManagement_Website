package com.rgbunny.service;

import com.rgbunny.dtos.UserResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponse GetMe(Authentication authentication);
}
