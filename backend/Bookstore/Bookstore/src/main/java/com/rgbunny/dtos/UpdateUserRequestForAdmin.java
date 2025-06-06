package com.rgbunny.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestForAdmin {
    @Size(min = 2, max = 20)
    private String userName;

    @Email
    @Size(max = 50)
    private String email;

    private Set<String> roles;
}
