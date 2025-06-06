package com.rgbunny.dtos;

import com.rgbunny.model.Book;

public class CartItemResponse {
    private Long cartItemId;
    private CartResponse cartResponse;
    private Book book;
    private Integer quantity;
    private Double discount;
    private Double bookPrice;
}
