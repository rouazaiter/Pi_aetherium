package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ModerationViolationServiceImpl {

    private static final int WARNING_THRESHOLD = 5;

    private final Map<Long, AtomicInteger> violationCounts = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> warnedUsers = new ConcurrentHashMap<>();

    private final MailDispatchService mailDispatchService;

    public ModerationViolationServiceImpl(MailDispatchService mailDispatchService) {
        this.mailDispatchService = mailDispatchService;
    }

    public void recordViolation(Long userId, List<String> detectedWords) {
        AtomicInteger count = violationCounts.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int newCount = count.incrementAndGet();

        System.out.println("[VIOLATION-DEBUG] userId=" + userId + " count=" + newCount + "/" + WARNING_THRESHOLD + " alreadyWarned=" + warnedUsers.getOrDefault(userId, false));

        if (newCount >= WARNING_THRESHOLD && !warnedUsers.getOrDefault(userId, false)) {
            warnedUsers.put(userId, true);
            System.out.println("[VIOLATION-DEBUG] Threshold reached — calling mailDispatchService.sendWarningEmail for userId=" + userId);
            mailDispatchService.sendWarningEmail(userId, newCount, detectedWords);
            System.out.println("[VIOLATION-DEBUG] mailDispatchService.sendWarningEmail call returned (async, not yet sent)");
        } else if (warnedUsers.getOrDefault(userId, false)) {
            System.out.println("[VIOLATION-DEBUG] Skipping email — user already warned");
        }
    }

    public void resetUser(Long userId) {
        violationCounts.remove(userId);
        warnedUsers.remove(userId);
        System.out.println("[VIOLATION-DEBUG] Reset userId=" + userId);
    }

    public int getViolationCount(Long userId) {
        return violationCounts.getOrDefault(userId, new AtomicInteger(0)).get();
    }
}
