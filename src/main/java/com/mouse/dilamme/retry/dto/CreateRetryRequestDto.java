package com.mouse.dilamme.retry.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRetryRequestDto {
    private String url;
    private String method;
    private String body;
    private Integer maxRetries;
    private Long backoffMs;
}