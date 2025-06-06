package com.rgbunny.service;

import com.rgbunny.repository.UserRepository;
import com.rgbunny.dtos.UpdateUserRequestForAdmin;
import com.rgbunny.dtos.UserResponseForAdmin;
import com.rgbunny.model.AppRole;
import com.rgbunny.model.Role;
import com.rgbunny.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    private Boolean CheckAdmin(Optional<User> user) {
        String appRoles = user.get().getRoles().stream()
                .map(role -> role.getRoleName().toString())
                .collect(Collectors.joining(","));
        return appRoles.contains("ROLE_ADMIN");
    }

    @Override
    public List<UserResponseForAdmin> GetAllUsers(Authentication authentication) {
        String userName = authentication.getName();
        Optional<User> user = userRepository.findByUserName(userName);
        if (user.isEmpty())
            return null;
        if (!CheckAdmin(user))
            return null;
        return userRepository.findAll().stream()
                .map(u -> modelMapper.map(u, UserResponseForAdmin.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseForAdmin> GetAllCustomers(Authentication authentication) {
        List<UserResponseForAdmin> users = GetAllUsers(authentication);
        List<UserResponseForAdmin> customers = users.stream().filter(user -> {
            String roles = user.getRoles().stream()
                    .map(role -> role.getRoleName().toString())
                    .collect(Collectors.joining(","));
            return (roles.contains("ROLE_USER"));
        }).toList();
        return customers;
    }

    @Override
    public List<UserResponseForAdmin> GetAllEmployees(Authentication authentication) {
        List<UserResponseForAdmin> users = GetAllUsers(authentication);
        List<UserResponseForAdmin> employees = users.stream().filter(user -> {
            String roles = user.getRoles().stream()
                    .map(role -> role.getRoleName().toString())
                    .collect(Collectors.joining(","));
            return (roles.contains("ROLE_EMPLOYEES"));
        }).toList();
        return employees;
    }

    @Override
    public List<UserResponseForAdmin> SearchUsersByName(Authentication authentication, String searchTerm) {
        List<UserResponseForAdmin> users = GetAllUsers(authentication);
        if (searchTerm.isEmpty())
            return users;
        List<UserResponseForAdmin> result = users.stream().filter(user -> {
            return user.getUserName().contains(searchTerm);
        }).toList();
        return result;
    }

    @Override
    public List<UserResponseForAdmin> SearchEmployeesByName(Authentication authentication, String searchTerm) {
        List<UserResponseForAdmin> users = GetAllEmployees(authentication);
        if (searchTerm.isEmpty())
            return users;
        List<UserResponseForAdmin> result = users.stream().filter(user -> {
            return user.getUserName().contains(searchTerm);
        }).toList();
        return result;
    }

    @Override
    public List<UserResponseForAdmin> SearchCustomersByName(Authentication authentication, String searchTerm) {
        List<UserResponseForAdmin> users = GetAllCustomers(authentication);
        if (searchTerm.isEmpty())
            return users;
        List<UserResponseForAdmin> result = users.stream().filter(user -> {
            return user.getUserName().contains(searchTerm);
        }).toList();
        return result;
    }

    @Override
    public UserResponseForAdmin UpdateUserById(Authentication authentication, Long updatedUserID,
            UpdateUserRequestForAdmin request) {
        String currentUserName = authentication.getName();
        Optional<User> currentUser = userRepository.findByUserName(currentUserName);
        if (currentUser.isEmpty())
            return null;
        if (!CheckAdmin(currentUser))
            return null;
        User user = userRepository.findById(updatedUserID)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getUserName().equals(request.getUserName())) {
            if (userRepository.existsByUserName(request.getUserName()))
                throw new RuntimeException("User name existed");
        }
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new RuntimeException("Email existed");
        }
        if (!(request.getUserName() == null))
            user.setUserName(request.getUserName());
        if (!(request.getEmail() == null))
            user.setEmail(request.getEmail());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleStr -> {
                        try {
                            AppRole appRole = AppRole.valueOf(roleStr);
                            return new Role(appRole);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid role: " + roleStr);
                        }
                    })
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }
        userRepository.save(user);

        return modelMapper.map(user, UserResponseForAdmin.class);
    }
}
