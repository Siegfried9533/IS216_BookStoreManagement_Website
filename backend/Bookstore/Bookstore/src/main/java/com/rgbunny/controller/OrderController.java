package com.rgbunny.controller;

import com.rgbunny.dtos.OrderRequest;
import com.rgbunny.dtos.OrderResponse;
import com.rgbunny.service.OrderService;
import com.rgbunny.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    @Autowired
    OrderService orderService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderResponse> orderBooks(@PathVariable String paymentMethod, @RequestBody OrderRequest orderRequest){
        String emailId = authUtil.loggedInEmail();
        OrderResponse order = orderService.placeOrder(
                emailId,
                orderRequest.getAddressId(),
                paymentMethod,
                orderRequest.getPgName(),
                orderRequest.getPgPaymentId(),
                orderRequest.getPgStatus(),
                orderRequest.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
