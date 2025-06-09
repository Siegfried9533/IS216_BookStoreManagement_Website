package com.rgbunny.controller;

import com.rgbunny.repository.RoleRepository;
import com.rgbunny.repository.UserRepository;
import com.rgbunny.model.AppRole;
import com.rgbunny.model.Role;
import com.rgbunny.model.User;
import com.rgbunny.security.jwt.JwtUtils;
import com.rgbunny.security.request.LoginRequest;
import com.rgbunny.security.request.ResetPasswordRequest;
import com.rgbunny.security.request.SignupRequest;
import com.rgbunny.security.response.LoginResponse;
import com.rgbunny.security.response.MessageResponse;
import com.rgbunny.security.services.UserDetailsImpl;
import com.rgbunny.security.services.EmailServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private EmailServiceImpl emailService;

    // Temporary endpoint to test password encoding
    @GetMapping("/encode-password")
    public String encodePassword(@RequestParam("password") String password) {
        System.out.println("Encoding password: " + password);
        String encodedPassword = encoder.encode(password);
        System.out.println("Encoded result: " + encodedPassword);
        return encodedPassword;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            System.out.println("=== START LOGIN REQUEST ===");
            System.out.println("Username: " + loginRequest.getUsername());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Content Type: " + request.getContentType());

            logger.info("Received signin request for username: {}", loginRequest.getUsername());
            logger.debug("Request headers: {}", Collections.list(request.getHeaderNames()));
            logger.debug("Request body: username={}", loginRequest.getUsername());

            // Check if user exists
            Optional<User> userOptional = userRepository.findByUserName(loginRequest.getUsername());
            if (userOptional.isEmpty()) {
                System.out.println("User not found: " + loginRequest.getUsername());
                logger.warn("User not found: {}", loginRequest.getUsername());
                Map<String, Object> map = new HashMap<>();
                map.put("message", "User not found");
                map.put("status", false);
                return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
            }

            Authentication authentication;
            try {
                System.out.println("Attempting to authenticate user...");
                logger.info("Attempting to authenticate user...");
                authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                                loginRequest.getPassword()));
                System.out.println("Authentication successful");
                logger.info("Authentication successful.");
            } catch (AuthenticationException exception) {
                System.out.println("Authentication failed: " + exception.getMessage());
                logger.error("Authentication failed: {}", exception.getMessage());
                Map<String, Object> map = new HashMap<>();
                map.put("message", "Bad credentials");
                map.put("status", false);
                return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            System.out.println("User authenticated successfully. User ID: " + userDetails.getId());
            logger.info("User authenticated successfully. User ID: {}", userDetails.getId());

            String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
            System.out.println("JWT token generated successfully");
            logger.info("JWT token generated successfully");

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            System.out.println("User roles: " + roles);
            logger.info("User roles: {}", roles);

            LoginResponse response = new LoginResponse(userDetails.getId(), userDetails.getUsername(), roles, jwtToken);
            System.out.println("Login response prepared successfully");
            logger.info("Login response prepared successfully");
            System.out.println("=== END LOGIN REQUEST ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ERROR IN LOGIN REQUEST ===");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== END ERROR LOG ===");

            logger.error("Unexpected error during authentication: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            errorResponse.put("status", false);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Received signup request for username: {}", signUpRequest.getUsername());

        try {
            if (userRepository.existsByUserName(signUpRequest.getUsername())) {
                logger.warn("Username is already taken: {}", signUpRequest.getUsername());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            // Create new user's account
            User user = new User(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);
                            break;
                        case "employee":
                            Role employeeRole = roleRepository.findByRoleName(AppRole.ROLE_EMPLOYEE)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(employeeRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }

            user.setRoles(roles);
            userRepository.save(user);
            logger.info("User registered successfully: {}", user.getUserName());

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: An unexpected error occurred during registration."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);

        try {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String token = UUID.randomUUID().toString();

                // Set token expiry to 1 hour
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                user.setResetPasswordToken(token);
                user.setResetPasswordExpiry(calendar.getTime());
                userRepository.save(user);

                try {
                    emailService.sendPasswordResetEmail(user.getEmail(), token);
                } catch (EmailServiceImpl.EmailSendingException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to send password reset email");
                }
            }
        } catch (EmailServiceImpl.EmailSendingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error sending reset email"));
        }

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(new MessageResponse("If your email exists, you'll receive a password reset link"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Validate input
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Password must be at least 6 characters"));
        }

        Optional<User> userOptional = userRepository.findByResetPasswordToken(request.getToken());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid or expired reset token"));
        }

        User user = userOptional.get();
        if (user.getResetPasswordExpiry().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Reset token has expired"));
        }

        // Update password
        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
