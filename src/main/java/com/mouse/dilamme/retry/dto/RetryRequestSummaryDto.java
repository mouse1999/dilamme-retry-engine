package com.mouse.dilamme.retry.dto;

import com.mouse.dilamme.retry.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RetryRequestSummaryDto {
    private UUID id;
    private String url;
    private String method;
    private int attemptCount;
    private RequestStatus status;
    private String lastError;
}