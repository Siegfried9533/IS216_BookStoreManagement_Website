package com.rgbunny.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private BookResponse book;
    private Integer quantity;
    private double discount;
    private double orderedBookPrice;
}
