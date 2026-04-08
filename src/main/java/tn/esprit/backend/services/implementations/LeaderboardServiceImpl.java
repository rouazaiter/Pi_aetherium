package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.backend.dto.LeaderboardEntryDto;
import tn.esprit.backend.dto.LeaderboardResponseDto;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ApplicationStatus;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.entities.ServiceRequestStatus;
import tn.esprit.backend.entities.User;
import tn.esprit.backend.repositories.ApplicationRepository;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.services.interfaces.LeaderboardService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final ApplicationRepository applicationRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public LeaderboardResponseDto getApplicantsLeaderboard(int days, int limit) {
        int safeDays = sanitizeDays(days);
        int safeLimit = sanitizeLimit(limit);

        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);
        List<Application> applications = applicationRepository.findByAppliedAtGreaterThanEqual(from);

        Map<Long, ApplicantAgg> byUser = new HashMap<>();
        for (Application app : applications) {
            User user = app.getApplicant();
            if (user == null || user.getId() == null) {
                continue;
            }

            ApplicantAgg agg = byUser.computeIfAbsent(user.getId(), id -> new ApplicantAgg(user));
            agg.total++;

            if (app.getStatus() == ApplicationStatus.ACCEPTED) {
                agg.success++;
            } else if (app.getStatus() == ApplicationStatus.PENDING) {
                agg.pending++;
            } else if (app.getStatus() == ApplicationStatus.REJECTED) {
                agg.failed++;
            }
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        for (ApplicantAgg agg : byUser.values()) {
            double successRate = agg.total == 0 ? 0d : (agg.success * 100.0) / agg.total;
            double score = (agg.success * 25.0) + (agg.pending * 5.0) + (agg.total * 1.5) - (agg.failed * 2.0);

            entries.add(LeaderboardEntryDto.builder()
                    .userId(agg.user.getId())
                    .username(agg.user.getUsername())
                    .email(agg.user.getEmail())
                    .totalCount(agg.total)
                    .successCount(agg.success)
                    .pendingCount(agg.pending)
                    .failedCount(agg.failed)
                    .successRate(round2(successRate))
                    .score(round2(score))
                    .build());
        }

        entries.sort(Comparator
                .comparingDouble(LeaderboardEntryDto::getScore).reversed()
                .thenComparingInt(LeaderboardEntryDto::getSuccessCount).reversed()
                .thenComparingInt(LeaderboardEntryDto::getTotalCount).reversed()
                .thenComparing(LeaderboardEntryDto::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)));

        List<LeaderboardEntryDto> ranked = applyRanksAndBadges(entries, safeLimit);

        return LeaderboardResponseDto.builder()
                .type("APPLICANTS")
                .days(safeDays)
                .limit(safeLimit)
                .generatedAt(LocalDateTime.now())
                .entries(ranked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderboardResponseDto getCreatorsLeaderboard(int days, int limit) {
        int safeDays = sanitizeDays(days);
        int safeLimit = sanitizeLimit(limit);

        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);
        List<ServiceRequest> requests = serviceRequestRepository.findByCreatedAtGreaterThanEqual(from);

        Map<Long, CreatorAgg> byUser = new HashMap<>();
        for (ServiceRequest request : requests) {
            User user = request.getCreator();
            if (user == null || user.getId() == null) {
                continue;
            }

            CreatorAgg agg = byUser.computeIfAbsent(user.getId(), id -> new CreatorAgg(user));
            agg.total++;

            if (request.getStatus() == ServiceRequestStatus.CLOSED) {
                agg.success++;
            } else if (request.getStatus() == ServiceRequestStatus.OPEN) {
                agg.pending++;
            } else if (request.getStatus() == ServiceRequestStatus.EXPIRED) {
                agg.failed++;
            }
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        for (CreatorAgg agg : byUser.values()) {
            double successRate = agg.total == 0 ? 0d : (agg.success * 100.0) / agg.total;
            double score = (agg.success * 30.0) + (agg.pending * 4.0) + (agg.total * 2.0) - (agg.failed * 3.0);

            entries.add(LeaderboardEntryDto.builder()
                    .userId(agg.user.getId())
                    .username(agg.user.getUsername())
                    .email(agg.user.getEmail())
                    .totalCount(agg.total)
                    .successCount(agg.success)
                    .pendingCount(agg.pending)
                    .failedCount(agg.failed)
                    .successRate(round2(successRate))
                    .score(round2(score))
                    .build());
        }

        entries.sort(Comparator
                .comparingDouble(LeaderboardEntryDto::getScore).reversed()
                .thenComparingInt(LeaderboardEntryDto::getSuccessCount).reversed()
                .thenComparingInt(LeaderboardEntryDto::getTotalCount).reversed()
                .thenComparing(LeaderboardEntryDto::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)));

        List<LeaderboardEntryDto> ranked = applyRanksAndBadges(entries, safeLimit);

        return LeaderboardResponseDto.builder()
                .type("CREATORS")
                .days(safeDays)
                .limit(safeLimit)
                .generatedAt(LocalDateTime.now())
                .entries(ranked)
                .build();
    }

    private List<LeaderboardEntryDto> applyRanksAndBadges(List<LeaderboardEntryDto> entries, int limit) {
        List<LeaderboardEntryDto> ranked = new ArrayList<>();
        int rank = 1;
        for (LeaderboardEntryDto entry : entries) {
            if (rank > limit) {
                break;
            }

            entry.setRank(rank);
            entry.setBadge(resolveBadge(rank));
            ranked.add(entry);
            rank++;
        }
        return ranked;
    }

    private String resolveBadge(int rank) {
        if (rank == 1) {
            return "LEGEND";
        }
        if (rank <= 3) {
            return "ELITE";
        }
        if (rank <= 10) {
            return "PRO";
        }
        return "RISING";
    }

    private int sanitizeDays(int days) {
        if (days < 1) {
            return 30;
        }
        return Math.min(days, 365);
    }

    private int sanitizeLimit(int limit) {
        if (limit < 1) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class ApplicantAgg {
        private final User user;
        private int total;
        private int success;
        private int pending;
        private int failed;

        private ApplicantAgg(User user) {
            this.user = user;
        }
    }

    private static class CreatorAgg {
        private final User user;
        private int total;
        private int success;
        private int pending;
        private int failed;

        private CreatorAgg(User user) {
            this.user = user;
        }
    }
}