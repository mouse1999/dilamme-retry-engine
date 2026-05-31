package com.dilamme.retryengine.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-process mock endpoints used by the test script.
 *
 * GET/POST /mock/target?id={key}
 * → Returns 503 for the first 3 calls with this key, then 200.
 * Simulates a flaky external service that eventually recovers.
 *
 * GET /mock/always-fail-4xx
 * → Always returns 404. Demonstrates that 4xx is never retried.
 *
 * DELETE /mock/reset?id={key}
 * → Resets the counter for a key (useful between test runs).
 *
 */
@Slf4j
@RestController
@RequestMapping("/mock")
public class MockTargetController {

    private static final int SUCESS_THRESHOLD = 3;
    private final ConcurrentHashMap<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();

    /**
     * Supports both GET and POST requests to simulate an eventually recovering, flaky service.
     */
    @RequestMapping(value = "/target", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> flakyTarget(
            @RequestParam(defaultValue = "default") String id,
            @RequestBody(required = false) Object body) {

        AtomicInteger counter = callCounts.computeIfAbsent(id, k -> new AtomicInteger(0));
        int callNumber = counter.incrementAndGet();

        log.info("[mock] /mock/target executed. id={}, callNumber={}", id, callNumber);

        if (callNumber <= SUCESS_THRESHOLD) {
            log.info("[mock] Returning 503 Service Unavailable (simulated failure {}/{})", callNumber, SUCESS_THRESHOLD);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Service temporarily unavailable",
                            "callNumber", callNumber,
                            "willSucceedAfter", SUCESS_THRESHOLD
                    ));
        }

        log.info("[mock] Returning 200 OK (recovered on call {})", callNumber);
        return ResponseEntity.ok(Map.of(
                "message", "Success!",
                "callNumber", callNumber
        ));
    }

    @GetMapping("/always-fail-4xx")
    public ResponseEntity<Map<String, Object>> alwaysFail4xx() {
        log.info("[mock] /mock/always-fail-4xx → 404 Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not found — this will never be retried"));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetCounter(@RequestParam(defaultValue = "default") String id) {
        callCounts.remove(id);
        log.info("[mock] Cleared call counter history for id={}", id);
        return ResponseEntity.ok(Map.of("reset", id));
    }
}