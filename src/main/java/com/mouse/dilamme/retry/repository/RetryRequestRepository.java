package com.mouse.dilamme.retry.repository;

import com.mouse.dilamme.retry.enums.RequestStatus;
import com.mouse.dilamme.retry.model.RetryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RetryRequestRepository extends JpaRepository<RetryRequest, UUID> {

    /**
     * Worker query: pick up all rows that are due for their next attempt.
     * Excludes terminal states (COMPLETED, FAILED) so dead-lettered jobs
     * are never touched again.
     */
    @Query("SELECT r FROM RetryRequest r WHERE r.status IN (:pending, :retrying) AND r.nextRetryAt <= :now")
    List<RetryRequest> findDueRequests(Instant now, RequestStatus pending, RequestStatus retrying);

    /**
     * Filter by status for GET /requests?status=
     */
    List<RetryRequest> findByStatus(RequestStatus status);
}
