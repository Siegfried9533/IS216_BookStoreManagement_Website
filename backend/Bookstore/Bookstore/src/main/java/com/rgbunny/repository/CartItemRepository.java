package com.rgbunny.repository;

import com.rgbunny.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.book.id = ?2")
    CartItem findCartItemByBookIdAndCartId(Long cartId, Long bookId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.book.id = ?2")
    void deleteCartItemByCartIdAndBookId(Long cartId, Long bookId);
}
