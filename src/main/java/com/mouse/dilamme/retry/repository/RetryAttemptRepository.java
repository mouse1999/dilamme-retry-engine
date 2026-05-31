package com.mouse.dilamme.retry.repository;

import com.mouse.dilamme.retry.model.RetryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RetryAttemptRepository extends JpaRepository<RetryAttempt, UUID> {
    List<RetryAttempt> findByRequestIdOrderByAttemptNumberAsc(UUID requestId);
}

