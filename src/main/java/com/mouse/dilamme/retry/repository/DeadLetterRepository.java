package com.mouse.dilamme.retry.repository;

import com.mouse.dilamme.retry.enums.DeadLetterReason;
import com.mouse.dilamme.retry.model.DeadLetterEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeadLetterRepository extends JpaRepository<DeadLetterEntry, UUID> {

    Optional<DeadLetterEntry> findByRequestId(UUID requestId);

    List<DeadLetterEntry> findAllByOrderByDeadLetteredAtDesc();

    List<DeadLetterEntry> findByReason(DeadLetterReason reason);
}
