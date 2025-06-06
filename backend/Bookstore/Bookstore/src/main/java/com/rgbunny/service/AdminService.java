package com.rgbunny.service;

import com.rgbunny.dtos.UpdateUserRequestForAdmin;
import com.rgbunny.dtos.UserResponseForAdmin;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AdminService {
    List<UserResponseForAdmin> GetAllUsers(Authentication authentication);
    List<UserResponseForAdmin> GetAllCustomers(Authentication authentication);
    List<UserResponseForAdmin> GetAllEmployees(Authentication authentication);
    List<UserResponseForAdmin> SearchUsersByName(Authentication authentication, String searchTerm);
    List<UserResponseForAdmin> SearchEmployeesByName(Authentication authentication, String searchTerm);
    List<UserResponseForAdmin> SearchCustomersByName(Authentication authentication, String searchTerm);
    UserResponseForAdmin UpdateUserById(Authentication authentication, Long updatedUserID, UpdateUserRequestForAdmin updateUserRequest);
}
