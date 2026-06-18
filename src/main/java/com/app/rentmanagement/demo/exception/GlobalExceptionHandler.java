package com.app.rentmanagement.demo.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.NOT_FOUND.toString())
                .errorMessage(ex.getMessage())
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicate(
            DuplicateRecordException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.CONFLICT.toString())
                .errorMessage(ex.getMessage())
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.BAD_REQUEST.toString())
                .errorMessage(ex.getMessage())
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .errorMessage(ex.getMessage())
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}