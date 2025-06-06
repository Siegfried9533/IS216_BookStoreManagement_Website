package com.rgbunny.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String email;
    private List<OrderItemResponse> orderItems;
    private LocalDate orderDate;
    private PaymentResponse payment;
    private Double totalAmount;
    private String orderStatus;
    private Long addressId;
}
