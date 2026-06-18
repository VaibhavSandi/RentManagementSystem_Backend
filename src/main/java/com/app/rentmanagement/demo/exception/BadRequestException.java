package com.app.rentmanagement.demo.exception;



public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}