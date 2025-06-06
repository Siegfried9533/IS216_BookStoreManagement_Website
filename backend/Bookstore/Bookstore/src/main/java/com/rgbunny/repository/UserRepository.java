package com.rgbunny.repository;

import com.rgbunny.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUserName(String userName);

    Boolean existsByEmail(String email);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);
}
