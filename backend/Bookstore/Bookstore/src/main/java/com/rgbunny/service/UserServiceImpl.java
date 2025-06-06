package com.rgbunny.service;

import com.rgbunny.repository.UserRepository;
import com.rgbunny.dtos.AddressDTO;
import com.rgbunny.dtos.UserResponse;
import com.rgbunny.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public UserResponse GetMe(Authentication authentication) {
        String userName = authentication.getName();
        Optional<User> user = userRepository.findByUserName(userName);
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        List<AddressDTO> addressDTOList = user.get().getAddresses().stream().map(address -> {
            return modelMapper.map(address, AddressDTO.class);
        }).toList();
        userResponse.setAddresses(addressDTOList);
        return userResponse;
    }
}
