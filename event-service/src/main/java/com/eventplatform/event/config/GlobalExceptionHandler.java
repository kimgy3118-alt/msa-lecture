package com.eventplatform.event.config;

import com.eventplatform.event.dto.EventDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EventDto.ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(EventDto.ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EventDto.ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EventDto.ApiResponse.error(message));
    }

    @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<EventDto.ApiResponse<Void>> handleIllegalState(
                IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(EventDto.ApiResponse.error(e.getMessage()));
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EventDto.ApiResponse<Void>> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EventDto.ApiResponse.error("서버 오류가 발생했습니다"));
    }
}
