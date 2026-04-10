package com.king.pos.Handllers;


public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}