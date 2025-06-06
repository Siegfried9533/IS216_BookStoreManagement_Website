package com.rgbunny.service;

import com.rgbunny.dtos.CartResponse;

public interface CartService {
    CartResponse addBookToCard(Long Id, Integer quantity);


    CartResponse getCart(String userEmail, Long cartId);

    CartResponse updateBookQuantityInCart(Long bookId, int updatedQuantity);

    String deleteBookInCart(Long bookId);
}
