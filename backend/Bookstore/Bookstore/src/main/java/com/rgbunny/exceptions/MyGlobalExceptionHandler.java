package com.rgbunny.exceptions;

import com.rgbunny.security.response.MessageResponse;
import com.rgbunny.security.services.EmailServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError)err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName,message);
        });
        return new ResponseEntity<Map<String,String>>(response,
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailServiceImpl.EmailSendingException.class)
    public ResponseEntity<?> handleEmailException(EmailServiceImpl.EmailSendingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error sending email"));
    }
}

