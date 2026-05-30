package com.mouse.dilamme.retry.service;

import com.mouse.dilamme.retry.dto.CreateRetryRequestDto;
import com.mouse.dilamme.retry.enums.DeadLetterReason;
import com.mouse.dilamme.retry.enums.RequestStatus;
import com.mouse.dilamme.retry.model.DeadLetterEntry;
import com.mouse.dilamme.retry.model.RetryAttempt;
import com.mouse.dilamme.retry.model.RetryRequest;
import com.mouse.dilamme.retry.repository.DeadLetterRepository;
import com.mouse.dilamme.retry.repository.RetryAttemptRepository;
import com.mouse.dilamme.retry.repository.RetryRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RetryRequestRepository requestRepo;
    private final RetryAttemptRepository attemptRepo;
    private final DeadLetterRepository deadLetterRepo;

    /**
     * Persist a new request via DTO payload and return it immediately with status=PENDING.
     * The worker will pick it up within 500ms.
     */
    @Transactional
    public RetryRequest create(CreateRetryRequestDto dto) {
        // Build the request fluently
        RetryRequest.RetryRequestBuilder builder = RetryRequest.builder()
                .url(dto.getUrl())
                .method(dto.getMethod() != null ? dto.getMethod().toUpperCase() : null)
                .body(dto.getBody());

        // Selectively apply configs only if explicitly passed in the DTO
        if (dto.getMaxRetries() != null) {
            builder.maxRetries(dto.getMaxRetries());
        }
        if (dto.getBackoffMs() != null) {
            builder.backoffMs(dto.getBackoffMs());
        }

        return requestRepo.save(builder.build());
    }

    /**
     * Return request + its full attempt history.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> findById(UUID id) {
        return requestRepo.findById(id).map(req -> {
            List<RetryAttempt> attempts = attemptRepo.findByRequestIdOrderByAttemptNumberAsc(id);
            return Map.of("request", req, "attempts", attempts);
        });
    }

    /**
     * Filter requests by status enum value.
     */
    @Transactional(readOnly = true)
    public List<RetryRequest> findByStatus(RequestStatus status) {
        return requestRepo.findByStatus(status);
    }

    /** All dead-letter entries, newest first. */
    @Transactional(readOnly = true)
    public List<DeadLetterEntry> findAllDeadLetters() {
        return deadLetterRepo.findAllByOrderByDeadLetteredAtDesc();
    }

    /** Dead-letter entries filtered by reason enum value. */
    @Transactional(readOnly = true)
    public List<DeadLetterEntry> findDeadLettersByReason(DeadLetterReason reason) {
        return deadLetterRepo.findByReason(reason);
    }

    /** Single dead-letter entry by its original requestId. */
    @Transactional(readOnly = true)
    public Optional<DeadLetterEntry> findDeadLetterByRequestId(UUID requestId) {
        return deadLetterRepo.findByRequestId(requestId);
    }
}