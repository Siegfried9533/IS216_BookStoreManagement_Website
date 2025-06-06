package com.rgbunny.service;

import com.rgbunny.repository.AddressRepository;
import com.rgbunny.repository.UserRepository;
import com.rgbunny.dtos.AddressDTO;
import com.rgbunny.dtos.AddressUpdateRequest;
import com.rgbunny.model.Address;
import com.rgbunny.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        Address addressResponse = addressRepository.save(address);
        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);
        return modelMapper.map(addressResponse, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressUpdateRequest addressUpdateRequest, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address's not found"));
        modelMapper.map(addressUpdateRequest, address);
        addressRepository.save(address);

        user.getAddresses().removeIf(addr -> addr.getId().equals(addressId));
        user.getAddresses().add(address);
        userRepository.save(user);

        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public String deleteAddress(Long addressId, User user) {
        addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address's not found"));
        addressRepository.deleteById(addressId);

        user.getAddresses().removeIf(addr -> addr.getId().equals(addressId));
        userRepository.save(user);

        return "Delete address successfully";
    }
}
