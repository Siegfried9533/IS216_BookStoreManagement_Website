package com.rgbunny.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateRequest {
    @Size(min = 5, message = "Tên đường phải có ít nhất 5 ký tự!")
    private String street;

    @Size(min = 5, message = "Tên tòa nhà phải có ít nhất 5 ký tự!")
    private String buildingName;

    @Size(min = 4, message = "Tên thành phố phải có ít nhất 4 ký tự!")
    private String city;

    @Size(min = 4, message = "Tên quận/huyện phải có ít nhất 4 ký tự!")
    private String district;

    @Size(min = 4, message = "Tên phường phải có ít nhất 4 ký tự!")
    private String ward;
}
