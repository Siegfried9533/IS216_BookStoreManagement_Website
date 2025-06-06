package com.rgbunny.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Address")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @NotBlank
    @Size(min = 5, message = "Tên đường phải có ít nhất 5 ký tự!")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Tên tòa nhà phải có ít nhất 5 ký tự!")
    private String buildingName;

    @NotBlank
    @Size(min = 4, message = "Tên thành phố phải có ít nhất 4 ký tự!")
    private String city;

    @NotBlank
    @Size(min = 4, message = "Tên quận/huyện phải có ít nhất 4 ký tự!")
    private String district;

    @NotBlank
    @Size(min = 4, message = "Tên quận/huyện phải có ít nhất 4 ký tự!")
    private String ward;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;
}
