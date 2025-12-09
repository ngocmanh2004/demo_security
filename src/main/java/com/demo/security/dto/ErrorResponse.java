package com.demo.security.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String message;
    private Integer status;
    private Long timestamp = System.currentTimeMillis();

    public ErrorResponse(String message) {
        this.message = message;
    }
}