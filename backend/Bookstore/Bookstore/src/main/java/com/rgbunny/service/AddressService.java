package com.rgbunny.service;

import com.rgbunny.dtos.AddressDTO;
import com.rgbunny.dtos.AddressUpdateRequest;
import com.rgbunny.model.User;
import jakarta.validation.Valid;

public interface AddressService {
    AddressDTO createAddress(@Valid AddressDTO addressDTO, User user);

    AddressDTO updateAddress(Long addressId, @Valid AddressUpdateRequest addressUpdateRequest, User user);

    String deleteAddress(Long addressId, User user);
}
