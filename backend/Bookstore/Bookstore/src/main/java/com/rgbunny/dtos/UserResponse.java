package com.rgbunny.dtos;

import com.rgbunny.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userName;
    private String email;
    private List<AddressDTO> addresses = new ArrayList<>();
}
