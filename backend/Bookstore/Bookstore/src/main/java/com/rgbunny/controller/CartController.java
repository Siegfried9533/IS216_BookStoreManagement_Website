package com.rgbunny.controller;

import com.rgbunny.repository.CartRepository;
import com.rgbunny.dtos.CartResponse;
import com.rgbunny.model.Cart;
import com.rgbunny.service.CartService;
import com.rgbunny.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;

    @PostMapping("/carts/books/{bookId}/quantity/{quantity}")
    public ResponseEntity<CartResponse> addProductToCart(@PathVariable Long bookId,
            @PathVariable Integer quantity) {
        CartResponse cartResponse = cartService.addBookToCard(bookId, quantity);
        return new ResponseEntity<CartResponse>(cartResponse, HttpStatus.CREATED);
    }

    @GetMapping("carts/users/cart")
    public ResponseEntity<CartResponse> getUsersCart() {
        String userEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);
        Long cartId = cart.getCartId();
        CartResponse cartResponse = cartService.getCart(userEmail, cartId);
        return new ResponseEntity<>(cartResponse, HttpStatus.OK);
    }

    @PatchMapping("carts/book/{bookId}/quantity/{operation}")
    public ResponseEntity<CartResponse> updateBookQuantityInCart(@PathVariable Long bookId,
            @PathVariable String operation) {
        CartResponse cartResponse = cartService.updateBookQuantityInCart(bookId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<>(cartResponse, HttpStatus.OK);
    }

    @DeleteMapping("carts/book/{bookId}")
    public ResponseEntity<String> deleteBookInCart(@PathVariable Long bookId) {
        String status = cartService.deleteBookInCart(bookId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
