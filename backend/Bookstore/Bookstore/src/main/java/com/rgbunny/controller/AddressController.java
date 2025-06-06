package com.rgbunny.controller;

import com.rgbunny.dtos.AddressDTO;
import com.rgbunny.dtos.AddressUpdateRequest;
import com.rgbunny.model.User;
import com.rgbunny.service.AddressService;
import com.rgbunny.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AddressController {
    @Autowired
    AddressService addressService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO address = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }

    @PatchMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest addressUpdateRequest) {
        User user = authUtil.loggedInUser();
        AddressDTO addressDTO = addressService.updateAddress(addressId, addressUpdateRequest, user);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> updateAddress(@PathVariable Long addressId) {
        User user = authUtil.loggedInUser();
        String message = addressService.deleteAddress(addressId, user);
        return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }
}
