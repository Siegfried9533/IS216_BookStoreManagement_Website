package com.rgbunny.dtos;

import com.rgbunny.model.Address;
import com.rgbunny.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseForAdmin {
    private String userName;
    private String email;
    private Set<Role> roles = new HashSet<>();
}
