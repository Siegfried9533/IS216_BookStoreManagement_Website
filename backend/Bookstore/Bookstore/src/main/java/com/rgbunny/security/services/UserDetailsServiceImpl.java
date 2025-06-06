package com.rgbunny.security.services;

import com.rgbunny.repository.UserRepository;
import com.rgbunny.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user by username: " + username);
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    System.out.println("User not found with username: " + username);
                    return new UsernameNotFoundException("User Not Found with username: " + username);
                });

        System.out.println("User found:");
        System.out.println("  ID: " + user.getId());
        System.out.println("  Username: " + user.getUserName());
        System.out.println("  Email: " + user.getEmail());
        // Avoid logging the raw password or encoded password directly here for security
        // System.out.println(" Password: [REDACTED]");

        return UserDetailsImpl.build(user);
    }

}
