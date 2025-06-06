package com.rgbunny.controller;

import com.rgbunny.dtos.UserResponse;
import com.rgbunny.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?>GetMe(Authentication authentication){
        UserResponse userResponse = userService.GetMe(authentication);
        if(userResponse == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userResponse);
    }
}
