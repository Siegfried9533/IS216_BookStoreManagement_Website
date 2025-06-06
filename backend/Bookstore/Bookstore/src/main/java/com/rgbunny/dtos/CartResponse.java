package com.rgbunny.dtos;

import com.rgbunny.model.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartId;
    private Double totalPrice = 0.0;
    private List<BookResponse> books = new ArrayList<>();
}
