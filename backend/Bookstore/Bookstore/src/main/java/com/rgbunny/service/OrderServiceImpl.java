package com.rgbunny.service;

import com.rgbunny.repository.*;
import com.rgbunny.dtos.OrderItemResponse;
import com.rgbunny.dtos.OrderResponse;
import com.rgbunny.model.*;
import com.rgbunny.exceptions.APIException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Transactional
    @Override
    public OrderResponse placeOrder(String emailId, Long addressId, String paymentMethod, String pgName,
            String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Not found cart");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found address"));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedBookPrice(cartItem.getBookPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Book book = item.getBook();

            // Reduce stock quantity
            book.setQuantity(book.getQuantity() - quantity);

            // Save product back to the database
            bookRepository.save(book);

            // Remove items from cart
            cartService.deleteBookInCart(item.getBook().getId());
        });

        OrderResponse orderResponse = modelMapper.map(savedOrder, OrderResponse.class);
        orderItems.forEach(item -> orderResponse.getOrderItems().add(modelMapper.map(item, OrderItemResponse.class)));

        orderResponse.setAddressId(addressId);

        return orderResponse;
    }
}
