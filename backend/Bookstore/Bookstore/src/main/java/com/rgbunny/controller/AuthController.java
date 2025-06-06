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
import jakarta.validation.Valid;
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
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Received signin request:");
        System.out.println("  Username: " + loginRequest.getUsername());
        // Avoid logging the raw password directly for security
        // System.out.println(" Password: " + loginRequest.getPassword());

        Authentication authentication;
        try {
            System.out.println("Attempting to authenticate user...");
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()));
            System.out.println("Authentication successful.");
        } catch (AuthenticationException exception) {
            System.out.println("Authentication failed: " + exception.getMessage());
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        LoginResponse response = new LoginResponse(userDetails.getId(), userDetails.getUsername(), roles, jwtToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken"));
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken"));
        }
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));
        Set<String> strRoles = signupRequest.getRole();
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
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found!"));
                        ;

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
        return ResponseEntity.ok(new MessageResponse("User register successfully"));
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
