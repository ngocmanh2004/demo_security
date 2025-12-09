package com.demo.security.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private String message;
    private Object data;
    private Integer status;

    public MessageResponse(String message) {
        this.message = message;
    }
}
