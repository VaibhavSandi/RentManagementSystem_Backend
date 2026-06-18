package com.app.rentmanagement.demo.exception;



import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {
    private String apiPath;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime errorTime;
}