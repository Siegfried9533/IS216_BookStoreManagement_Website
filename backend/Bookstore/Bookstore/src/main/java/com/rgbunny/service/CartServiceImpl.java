package com.rgbunny.service;

import com.rgbunny.repository.BookRepository;
import com.rgbunny.repository.CartItemRepository;
import com.rgbunny.repository.CartRepository;
import com.rgbunny.dtos.BookResponse;
import com.rgbunny.dtos.CartResponse;
import com.rgbunny.model.Book;
import com.rgbunny.model.Cart;
import com.rgbunny.model.CartItem;
import com.rgbunny.exceptions.APIException;
import com.rgbunny.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null)
            return userCart;
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);
        return newCart;
    }

    @Override
    public CartResponse addBookToCard(Long bookId, Integer quantity) {
        // find existing cart or create new one
        Cart cart = createCart();
        // retrieve book
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Not found book"));
        // perform validations
        CartItem cartItem = cartItemRepository.findCartItemByBookIdAndCartId(cart.getCartId(), book.getId());
        if (cartItem != null)
            throw new APIException("Book" + book.getTitle() + "already exist in the cart");
        if (book.getQuantity() == 0)
            throw new APIException("Book" + book.getTitle() + "is not available");
        if (book.getQuantity() < quantity)
            throw new APIException("Please, make an order of the book" + book.getTitle()
                    + "less than or equal to the quantity" + book.getQuantity());
        // create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setBook(book);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(book.getDiscount());
        newCartItem.setBookPrice(book.getPrice());
        cartItemRepository.save(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + book.getPrice() * (1 - book.getDiscount()) * quantity);
        cartRepository.save(cart);
        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);

        List<CartItem> cartItems = cart.getCartItems();
        Stream<BookResponse> bookStream = cartItems.stream().map(item -> {
            BookResponse map = modelMapper.map(item.getBook(), BookResponse.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartResponse.setBooks(bookStream.toList());
        return cartResponse;
    }

    @Override
    public CartResponse getCart(String userEmail, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(userEmail, cartId);
        if (cart == null)
            throw new ResourceNotFoundException();

        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        cart.getCartItems().forEach(c -> {
            c.getBook().setQuantity(c.getQuantity());
        });
        List<BookResponse> books = cart.getCartItems().stream().map(ci -> {
            BookResponse bookResponse = modelMapper.map(ci.getBook(), BookResponse.class);
            bookResponse.setQuantity(ci.getQuantity());
            return bookResponse;
        }).toList();
        cartResponse.setBooks(books);
        return cartResponse;
    }

    @Transactional
    @Override
    public CartResponse updateBookQuantityInCart(Long bookId, int updatedQuantity) {
        String userEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);
        if (cart == null)
            throw new APIException("Not Found");
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Not found book"));
        Long cartId = cart.getCartId();
        CartItem cartItem = cartItemRepository.findCartItemByBookIdAndCartId(cartId, bookId);
        if (cartItem == null)
            throw new APIException("Not Found");
        if (book.getQuantity() == 0)
            throw new APIException("Book" + book.getTitle() + "is not available");
        if (cartItem.getQuantity() + updatedQuantity > book.getQuantity())
            throw new APIException("Please, make an order of the book" + book.getTitle()
                    + "less than or equal to the quantity" + book.getQuantity());

        cart.setTotalPrice(
                cart.getTotalPrice() - cartItem.getBookPrice() * (1 - cartItem.getDiscount()) * cartItem.getQuantity());

        cartItem.setQuantity(cartItem.getQuantity() + updatedQuantity);
        cartItem.setBookPrice(book.getPrice());
        cartItem.setDiscount(book.getDiscount());

        cart.setTotalPrice(
                cart.getTotalPrice() + cartItem.getBookPrice() * (1 - cartItem.getDiscount()) * cartItem.getQuantity());

        cartRepository.save(cart);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0)
            cartItemRepository.deleteCartItemByCartIdAndBookId(cartId, bookId);

        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        List<BookResponse> bookResponses = cart.getCartItems().stream().map(ci -> {
            BookResponse bookResponse = modelMapper.map(ci.getBook(), BookResponse.class);
            bookResponse.setQuantity(ci.getQuantity());
            return bookResponse;
        }).toList();
        cartResponse.setBooks(bookResponses);
        return cartResponse;
    }

    @Transactional
    @Override
    public String deleteBookInCart(Long bookId) {
        String email = authUtil.loggedInEmail();

        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null)
            throw new ResourceNotFoundException("Cart not found");
        Long cartId = cart.getCartId();

        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        CartItem cartItem = cartItemRepository.findCartItemByBookIdAndCartId(cartId, bookId);
        if (cartItem == null)
            throw new ResourceNotFoundException("Cart item not found");

        cart.setTotalPrice(
                cart.getTotalPrice() - cartItem.getBookPrice() * cartItem.getQuantity() * (1 - cartItem.getDiscount()));

        cartItemRepository.deleteCartItemByCartIdAndBookId(cartId, bookId);
        return "Book " + book.getTitle() + " removed from cart";
    }
}
