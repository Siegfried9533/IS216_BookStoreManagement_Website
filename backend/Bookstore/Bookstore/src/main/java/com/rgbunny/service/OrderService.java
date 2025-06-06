package com.rgbunny.service;

import com.rgbunny.dtos.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
