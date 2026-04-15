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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final double REAL_SCORE_WEIGHT = 0.85;
    private static final double AI_QUALITY_WEIGHT = 0.15;
    private static final double LOG_BASE_20 = Math.log1p(20.0);

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "and", "or", "for", "to", "in", "on", "of", "with", "at", "by", "from", "is", "are", "be",
        "de", "la", "le", "les", "des", "du", "un", "une", "et", "ou", "pour", "dans", "sur", "avec", "par", "est", "sont"
    ));

    private static final Set<String> PROFESSIONAL_TERMS = new HashSet<>(Arrays.asList(
        "hello", "hi", "dear", "regards", "sincerely", "thank", "thanks", "please",
        "bonjour", "salut", "cordialement", "merci", "madame", "monsieur", "svp"
    ));

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
            agg.aiQualitySum += computeAiQualityScore(app.getMessage(), app.getServiceRequest());
            agg.aiQualityCount++;

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
            double aiQualityScore = agg.aiQualityCount == 0 ? 0d : agg.aiQualitySum / agg.aiQualityCount;
            double realScore = computeApplicantRealScore(agg.success, agg.failed, agg.total);
            double score = (REAL_SCORE_WEIGHT * realScore) + (AI_QUALITY_WEIGHT * aiQualityScore);

            entries.add(LeaderboardEntryDto.builder()
                    .userId(agg.user.getId())
                    .username(agg.user.getUsername())
                    .email(agg.user.getEmail())
                    .totalCount(agg.total)
                    .successCount(agg.success)
                    .pendingCount(agg.pending)
                    .failedCount(agg.failed)
                    .successRate(round2(successRate))
                    .realScore(round2(realScore))
                    .aiQualityScore(round2(aiQualityScore))
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
        List<Application> applications = applicationRepository.findByAppliedAtGreaterThanEqual(from);

        Map<Long, ServiceRequest> requestById = requests.stream()
            .filter(r -> r.getId() != null)
            .collect(Collectors.toMap(ServiceRequest::getId, r -> r));

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

        for (Application app : applications) {
            ServiceRequest linkedRequest = app.getServiceRequest();
            if (linkedRequest == null || linkedRequest.getId() == null) {
                continue;
            }

            ServiceRequest selectedRequest = requestById.get(linkedRequest.getId());
            if (selectedRequest == null || selectedRequest.getCreator() == null || selectedRequest.getCreator().getId() == null) {
                continue;
            }

            CreatorAgg agg = byUser.get(selectedRequest.getCreator().getId());
            if (agg == null) {
                continue;
            }

            agg.applicationsReceived++;
            agg.aiQualitySum += computeAiQualityScore(app.getMessage(), selectedRequest);
            agg.aiQualityCount++;
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        for (CreatorAgg agg : byUser.values()) {
            double successRate = agg.total == 0 ? 0d : (agg.success * 100.0) / agg.total;
            double avgApplicationsPerRequest = agg.total == 0 ? 0d : (double) agg.applicationsReceived / agg.total;
            double aiQualityScore = agg.aiQualityCount == 0 ? 0d : agg.aiQualitySum / agg.aiQualityCount;
            double realScore = computeCreatorRealScore(agg.success, agg.failed, agg.total, avgApplicationsPerRequest);
            double score = (REAL_SCORE_WEIGHT * realScore) + (AI_QUALITY_WEIGHT * aiQualityScore);

            entries.add(LeaderboardEntryDto.builder()
                    .userId(agg.user.getId())
                    .username(agg.user.getUsername())
                    .email(agg.user.getEmail())
                    .totalCount(agg.total)
                    .successCount(agg.success)
                    .pendingCount(agg.pending)
                    .failedCount(agg.failed)
                    .successRate(round2(successRate))
                    .realScore(round2(realScore))
                    .aiQualityScore(round2(aiQualityScore))
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

    private double computeApplicantRealScore(int success, int failed, int total) {
        if (total == 0) {
            return 0d;
        }

        double successRate = (success * 100.0) / total;
        double rejectionRate = (failed * 100.0) / total;
        double engagement = Math.min(100d, (Math.log1p(total) / LOG_BASE_20) * 100.0);

        double score = (0.75 * successRate) + (0.20 * engagement) - (0.15 * rejectionRate);
        return clamp(score, 0d, 100d);
    }

    private double computeCreatorRealScore(int success, int failed, int total, double avgApplicationsPerRequest) {
        if (total == 0) {
            return 0d;
        }

        double closureRate = (success * 100.0) / total;
        double expiryRate = (failed * 100.0) / total;
        double throughput = Math.min(100d, (Math.log1p(total) / LOG_BASE_20) * 100.0);
        double attractiveness = Math.min(100d, (avgApplicationsPerRequest / 10.0) * 100.0);

        double score = (0.60 * closureRate) + (0.25 * attractiveness) + (0.20 * throughput) - (0.25 * expiryRate);
        return clamp(score, 0d, 100d);
    }

    private double computeAiQualityScore(String message, ServiceRequest serviceRequest) {
        String text = message == null ? "" : message.trim();
        if (text.isEmpty()) {
            return 0d;
        }

        double clarity = clarityScore(text);
        double relevance = relevanceScore(text, serviceRequest);
        double structure = structureScore(text);
        double professionalism = professionalismScore(text);

        double quality = (0.40 * relevance) + (0.30 * clarity) + (0.20 * structure) + (0.10 * professionalism);
        return clamp(quality, 0d, 100d);
    }

    private double clarityScore(String text) {
        int words = countWords(text);
        if (words < 8) {
            return 15d;
        }
        if (words < 20) {
            return 35d;
        }
        if (words <= 220) {
            return 85d;
        }
        if (words <= 350) {
            return 70d;
        }
        return 50d;
    }

    private double relevanceScore(String text, ServiceRequest request) {
        if (request == null) {
            return 50d;
        }

        String scope = (request.getName() == null ? "" : request.getName()) + " " +
                (request.getDescription() == null ? "" : request.getDescription());

        Set<String> messageKeywords = extractKeywords(text, 30);
        Set<String> requestKeywords = extractKeywords(scope, 30);

        if (requestKeywords.isEmpty() || messageKeywords.isEmpty()) {
            return 50d;
        }

        long overlap = requestKeywords.stream().filter(messageKeywords::contains).count();
        double ratio = overlap / (double) requestKeywords.size();

        return clamp(35d + (ratio * 65d), 0d, 100d);
    }

    private double structureScore(String text) {
        int sentences = splitSentences(text).size();
        int paragraphs = text.split("\\r?\\n").length;
        boolean hasGreeting = containsAny(text, PROFESSIONAL_TERMS);

        double sentenceScore = sentences >= 4 ? 55d : (sentences >= 2 ? 35d : 15d);
        double paragraphScore = paragraphs >= 2 ? 30d : 15d;
        double greetingScore = hasGreeting ? 15d : 0d;

        return clamp(sentenceScore + paragraphScore + greetingScore, 0d, 100d);
    }

    private double professionalismScore(String text) {
        boolean hasPoliteWords = containsAny(text, PROFESSIONAL_TERMS);
        double uppercaseRatio = uppercaseRatio(text);
        boolean hasRepeatedSymbols = text.matches(".*([!?.])\\1{2,}.*");

        double score = hasPoliteWords ? 75d : 55d;
        if (uppercaseRatio > 0.30) {
            score -= 20d;
        }
        if (hasRepeatedSymbols) {
            score -= 10d;
        }
        return clamp(score, 0d, 100d);
    }

    private List<String> splitSentences(String text) {
        return Arrays.stream(text.split("[.!?]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private int countWords(String text) {
        String[] parts = text.trim().split("\\s+");
        int count = 0;
        for (String p : parts) {
            if (!p.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private Set<String> extractKeywords(String text, int max) {
        String[] tokens = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+");

        Set<String> keywords = new HashSet<>();
        for (String token : tokens) {
            if (token.isBlank() || token.length() < 3 || STOPWORDS.contains(token)) {
                continue;
            }
            keywords.add(token);
            if (keywords.size() >= max) {
                break;
            }
        }
        return keywords;
    }

    private boolean containsAny(String text, Set<String> terms) {
        String lower = text.toLowerCase();
        return terms.stream().anyMatch(lower::contains);
    }

    private double uppercaseRatio(String text) {
        long letters = text.chars().filter(Character::isLetter).count();
        if (letters == 0) {
            return 0d;
        }
        long uppercase = text.chars().filter(Character::isUpperCase).count();
        return uppercase / (double) letters;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class ApplicantAgg {
        private final User user;
        private int total;
        private int success;
        private int pending;
        private int failed;
        private double aiQualitySum;
        private int aiQualityCount;

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
        private int applicationsReceived;
        private double aiQualitySum;
        private int aiQualityCount;

        private CreatorAgg(User user) {
            this.user = user;
        }
    }
}