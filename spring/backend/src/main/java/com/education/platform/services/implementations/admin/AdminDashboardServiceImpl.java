package com.education.platform.services.implementations.admin;

import com.education.platform.common.ApiException;
import com.education.platform.dto.admin.dashboard.AdminCvSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardActivityResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminMonthlyActivityPointResponse;
import com.education.platform.dto.admin.dashboard.AdminPageResponse;
import com.education.platform.dto.admin.dashboard.AdminPortfolioSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminRecentActivityResponse;
import com.education.platform.dto.admin.dashboard.AdminStatusCountResponse;
import com.education.platform.dto.admin.dashboard.AdminSubscriptionSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminUserDetailResponse;
import com.education.platform.dto.admin.dashboard.AdminUserSummaryResponse;
import com.education.platform.entities.AccountStatus;
import com.education.platform.entities.Reclamation;
import com.education.platform.entities.ReclamationStatus;
import com.education.platform.entities.Role;
import com.education.platform.entities.Subscription;
import com.education.platform.entities.SubscriptionStatus;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.ReclamationRepository;
import com.education.platform.repositories.SubscriptionRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.cv.CVProfileRepository;
import com.education.platform.repositories.portfolio.PortfolioCollectionRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.admin.AdminDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final PortfolioCollectionRepository portfolioCollectionRepository;
    private final CVProfileRepository cvProfileRepository;
    private final CVDraftRepository cvDraftRepository;
    private final ReclamationRepository reclamationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminDashboardServiceImpl(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            PortfolioCollectionRepository portfolioCollectionRepository,
            CVProfileRepository cvProfileRepository,
            CVDraftRepository cvDraftRepository,
            ReclamationRepository reclamationRepository,
            SubscriptionRepository subscriptionRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.portfolioCollectionRepository = portfolioCollectionRepository;
        this.cvProfileRepository = cvProfileRepository;
        this.cvDraftRepository = cvDraftRepository;
        this.reclamationRepository = reclamationRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        requireAdmin();

        List<User> users = userRepository.findAll();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<PortfolioProject> projects = portfolioProjectRepository.findAll();
        List<PortfolioCollection> collections = portfolioCollectionRepository.findAll();
        List<CVProfile> cvProfiles = cvProfileRepository.findAll();
        List<CVDraft> cvDrafts = cvDraftRepository.findAll();
        List<Reclamation> reclamations = reclamationRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();

        long totalUsers = users.size();
        long totalStudents = users.stream().filter(user -> user.getRole() == Role.USER).count();
        long totalAdmins = users.stream().filter(user -> user.getRole() == Role.ADMIN).count();
        long activeSubscriptions = subscriptions.stream().filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE).count();
        long pendingReclamations = reclamations.stream().filter(reclamation -> reclamation.getStatus() == ReclamationStatus.PENDING).count();
        long pendingPortfolioActions = portfolios.stream().filter(item -> item.getModerationStatus() == ModerationStatus.UNDER_REVIEW).count();
        long pendingProjectActions = projects.stream().filter(item -> item.getModerationStatus() == ModerationStatus.UNDER_REVIEW).count();
        long pendingCollectionActions = collections.stream().filter(item -> item.getModerationStatus() == ModerationStatus.UNDER_REVIEW).count();

        return AdminDashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalAdmins(totalAdmins)
                .totalPortfolios(portfolios.size())
                .totalCvProfiles(cvProfiles.size())
                .totalCvDrafts(cvDrafts.size())
                .totalReclamations(reclamations.size())
                .totalSubscriptions(subscriptions.size())
                .activeSubscriptions(activeSubscriptions)
                .pendingReclamations(pendingReclamations)
                .pendingAdminActions(pendingReclamations + pendingPortfolioActions + pendingProjectActions + pendingCollectionActions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardActivityResponse getActivity() {
        requireAdmin();

        List<User> users = userRepository.findAll();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<CVDraft> cvDrafts = cvDraftRepository.findAll();
        List<Reclamation> reclamations = reclamationRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();

        List<AdminMonthlyActivityPointResponse> monthlyActivity = buildMonthlyActivity(users, portfolios, cvDrafts, reclamations, subscriptions);
        List<AdminStatusCountResponse> reclamationStatuses = Stream.of(ReclamationStatus.values())
                .map(status -> AdminStatusCountResponse.builder()
                        .status(status.name())
                        .count(reclamations.stream().filter(item -> item.getStatus() == status).count())
                        .build())
                .toList();

        List<AdminRecentActivityResponse> recentActivity = buildRecentActivity(users, portfolios, reclamations, subscriptions);

        return AdminDashboardActivityResponse.builder()
                .monthlyActivity(monthlyActivity)
                .reclamationsByStatus(reclamationStatuses)
                .recentActivity(recentActivity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPageResponse<AdminUserSummaryResponse> listUsers(String q, String status, String role, Integer page, Integer size) {
        requireAdmin();

        List<AdminUserSummaryResponse> users = userRepository.findAll().stream()
                .filter(user -> matchesUserQuery(user, q))
                .filter(user -> matchesAccountStatus(user, status))
                .filter(user -> matchesRole(user, role))
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(User::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toUserSummary)
                .toList();

        return page(users, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        long portfolioCount = portfolioRepository.findAll().stream()
                .filter(item -> sameUserId(item.getUser(), userId))
                .count();
        long cvProfileCount = cvProfileRepository.findAll().stream()
                .filter(item -> sameUserId(item.getUser(), userId))
                .count();
        long cvDraftCount = cvDraftRepository.findAll().stream()
                .filter(item -> sameUserId(item.getUser(), userId))
                .count();
        long reclamationCount = reclamationRepository.findAll().stream()
                .filter(item -> sameUserId(item.getUser(), userId))
                .count();
        long subscriptionCount = subscriptionRepository.findAll().stream()
                .filter(item -> sameUserId(item.getUser(), userId))
                .count();

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(profileFirstName(user))
                .lastName(profileLastName(user))
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .portfolioCount(portfolioCount)
                .cvProfileCount(cvProfileCount)
                .cvDraftCount(cvDraftCount)
                .reclamationCount(reclamationCount)
                .subscriptionCount(subscriptionCount)
                .build();
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse updateUserStatus(Long userId, boolean active) {
        User admin = requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!active && Objects.equals(admin.getId(), user.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own admin account");
        }

        user.setAccountStatus(active ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED);
        userRepository.save(user);
        return toUserSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPageResponse<AdminPortfolioSummaryResponse> listPortfolios(String q, String visibility, String moderationStatus, Integer page, Integer size) {
        requireAdmin();

        List<AdminPortfolioSummaryResponse> items = portfolioRepository.findAll().stream()
                .filter(item -> matchesPortfolioQuery(item, q))
                .filter(item -> matchesEnumName(item.getVisibility(), visibility))
                .filter(item -> matchesEnumName(item.getModerationStatus(), moderationStatus))
                .sorted(Comparator.comparing(Portfolio::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Portfolio::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toPortfolioSummary)
                .toList();

        return page(items, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPageResponse<AdminCvSummaryResponse> listCvs(String q, Integer page, Integer size) {
        requireAdmin();

        Map<Long, CVProfile> profilesByUserId = cvProfileRepository.findAll().stream()
                .filter(profile -> profile.getUser() != null && profile.getUser().getId() != null)
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<CVDraft>> draftsByUserId = cvDraftRepository.findAll().stream()
                .filter(draft -> draft.getUser() != null && draft.getUser().getId() != null)
                .collect(Collectors.groupingBy(draft -> draft.getUser().getId(), LinkedHashMap::new, Collectors.toList()));

        List<Long> userIds = Stream.concat(profilesByUserId.keySet().stream(), draftsByUserId.keySet().stream())
                .distinct()
                .toList();

        List<AdminCvSummaryResponse> rows = userIds.stream()
                .map(userId -> {
                    User user = profilesByUserId.containsKey(userId)
                            ? profilesByUserId.get(userId).getUser()
                            : draftsByUserId.get(userId).get(0).getUser();
                    CVProfile profile = profilesByUserId.get(userId);
                    List<CVDraft> drafts = draftsByUserId.getOrDefault(userId, List.of());
                    LocalDateTime latestDraftUpdatedAt = drafts.stream()
                            .map(CVDraft::getUpdatedAt)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    return AdminCvSummaryResponse.builder()
                            .userId(userId)
                            .profileId(profile != null ? profile.getId() : null)
                            .username(user.getUsername())
                            .fullName(fullName(user))
                            .headline(profile != null ? profile.getHeadline() : null)
                            .visibility(profile != null ? profile.getVisibility() : null)
                            .preferredTemplate(profile != null ? profile.getPreferredTemplate() : null)
                            .draftCount(drafts.size())
                            .profileUpdatedAt(profile != null ? profile.getUpdatedAt() : null)
                            .latestDraftUpdatedAt(latestDraftUpdatedAt)
                            .build();
                })
                .filter(item -> matchesCvQuery(item, q))
                .sorted(Comparator.comparing(AdminCvSummaryResponse::getLatestDraftUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminCvSummaryResponse::getProfileUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return page(rows, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPageResponse<AdminSubscriptionSummaryResponse> listSubscriptions(String q, String status, Integer page, Integer size) {
        requireAdmin();

        List<AdminSubscriptionSummaryResponse> items = subscriptionRepository.findAll().stream()
                .filter(item -> matchesSubscriptionQuery(item, q))
                .filter(item -> matchesEnumName(item.getStatus(), status))
                .sorted(Comparator.comparing(Subscription::getDateOfSubscription, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Subscription::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toSubscriptionSummary)
                .toList();

        return page(items, page, size);
    }

    private User requireAdmin() {
        User current = currentUserService.getCurrentUser();
        if (current.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return current;
    }

    private List<AdminMonthlyActivityPointResponse> buildMonthlyActivity(
            List<User> users,
            List<Portfolio> portfolios,
            List<CVDraft> drafts,
            List<Reclamation> reclamations,
            List<Subscription> subscriptions) {
        YearMonth currentMonth = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(currentMonth.minusMonths(i));
        }

        return months.stream()
                .map(month -> AdminMonthlyActivityPointResponse.builder()
                        .label(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .users(countByInstantMonth(users, month, User::getCreatedAt))
                        .portfolios(countByLocalDateTimeMonth(portfolios, month, Portfolio::getCreatedAt))
                        .cvDrafts(countByLocalDateTimeMonth(drafts, month, CVDraft::getCreatedAt))
                        .reclamations(countByInstantMonth(reclamations, month, Reclamation::getCreatedAt))
                        .subscriptions(countByInstantMonth(subscriptions, month, subscription -> {
                            LocalDate value = subscription.getDateOfSubscription();
                            return value != null ? value.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
                        }))
                        .build())
                .toList();
    }

    private List<AdminRecentActivityResponse> buildRecentActivity(
            List<User> users,
            List<Portfolio> portfolios,
            List<Reclamation> reclamations,
            List<Subscription> subscriptions) {
        List<AdminRecentActivityResponse> items = new ArrayList<>();

        users.stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .map(user -> AdminRecentActivityResponse.builder()
                        .type("USER")
                        .title(user.getUsername())
                        .subtitle("New user account")
                        .status(user.getAccountStatus() != null ? user.getAccountStatus().name() : null)
                        .createdAt(user.getCreatedAt())
                        .build())
                .forEach(items::add);

        portfolios.stream()
                .sorted(Comparator.comparing(Portfolio::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .map(portfolio -> AdminRecentActivityResponse.builder()
                        .type("PORTFOLIO")
                        .title(hasText(portfolio.getTitle()) ? portfolio.getTitle() : "Untitled portfolio")
                        .subtitle(portfolio.getUser() != null ? portfolio.getUser().getUsername() : "Unknown owner")
                        .status(portfolio.getModerationStatus() != null ? portfolio.getModerationStatus().name() : null)
                        .createdAt(toInstant(portfolio.getCreatedAt()))
                        .build())
                .forEach(items::add);

        reclamations.stream()
                .sorted(Comparator.comparing(Reclamation::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .map(reclamation -> AdminRecentActivityResponse.builder()
                        .type("RECLAMATION")
                        .title(reclamation.getSubject())
                        .subtitle(reclamation.getUser() != null ? reclamation.getUser().getUsername() : "Unknown user")
                        .status(reclamation.getStatus() != null ? reclamation.getStatus().name() : null)
                        .createdAt(reclamation.getCreatedAt())
                        .build())
                .forEach(items::add);

        subscriptions.stream()
                .sorted(Comparator.comparing(Subscription::getDateOfSubscription, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .map(subscription -> AdminRecentActivityResponse.builder()
                        .type("SUBSCRIPTION")
                        .title(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().name() : "Subscription")
                        .subtitle(subscription.getUser() != null ? subscription.getUser().getUsername() : "Unknown user")
                        .status(subscription.getStatus() != null ? subscription.getStatus().name() : null)
                        .createdAt(subscription.getDateOfSubscription() != null
                                ? subscription.getDateOfSubscription().atStartOfDay().toInstant(ZoneOffset.UTC)
                                : null)
                        .build())
                .forEach(items::add);

        return items.stream()
                .sorted(Comparator.comparing(AdminRecentActivityResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .toList();
    }

    private AdminUserSummaryResponse toUserSummary(User user) {
        return AdminUserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(profileFirstName(user))
                .lastName(profileLastName(user))
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private AdminPortfolioSummaryResponse toPortfolioSummary(Portfolio portfolio) {
        return AdminPortfolioSummaryResponse.builder()
                .id(portfolio.getId())
                .title(hasText(portfolio.getTitle()) ? portfolio.getTitle() : "Untitled portfolio")
                .ownerUsername(portfolio.getUser() != null ? portfolio.getUser().getUsername() : null)
                .ownerName(fullName(portfolio.getUser()))
                .visibility(portfolio.getVisibility())
                .moderationStatus(portfolio.getModerationStatus())
                .projectCount(portfolio.getProjects() != null ? portfolio.getProjects().size() : 0)
                .totalViews(portfolio.getTotalViews() != null ? portfolio.getTotalViews() : 0)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    private AdminSubscriptionSummaryResponse toSubscriptionSummary(Subscription subscription) {
        User user = subscription.getUser();
        return AdminSubscriptionSummaryResponse.builder()
                .id(subscription.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .subscriptionPlan(subscription.getSubscriptionPlan())
                .status(subscription.getStatus())
                .dateOfSubscription(subscription.getDateOfSubscription())
                .expirationDate(subscription.getExpirationDate())
                .billingDate(subscription.getBillingDate())
                .autoRenew(Boolean.TRUE.equals(subscription.getAutoRenew()))
                .build();
    }

    private boolean matchesUserQuery(User user, String q) {
        if (!hasText(q)) {
            return true;
        }
        String query = q.trim().toLowerCase(Locale.ENGLISH);
        return containsIgnoreCase(user.getUsername(), query)
                || containsIgnoreCase(user.getEmail(), query)
                || containsIgnoreCase(profileFirstName(user), query)
                || containsIgnoreCase(profileLastName(user), query);
    }

    private boolean matchesAccountStatus(User user, String status) {
        return matchesEnumName(user.getAccountStatus(), status);
    }

    private boolean matchesRole(User user, String role) {
        return matchesEnumName(user.getRole(), role);
    }

    private boolean matchesPortfolioQuery(Portfolio portfolio, String q) {
        if (!hasText(q)) {
            return true;
        }
        String query = q.trim().toLowerCase(Locale.ENGLISH);
        return containsIgnoreCase(portfolio.getTitle(), query)
                || containsIgnoreCase(portfolio.getBio(), query)
                || containsIgnoreCase(portfolio.getUser() != null ? portfolio.getUser().getUsername() : null, query)
                || containsIgnoreCase(fullName(portfolio.getUser()), query);
    }

    private boolean matchesCvQuery(AdminCvSummaryResponse item, String q) {
        if (!hasText(q)) {
            return true;
        }
        String query = q.trim().toLowerCase(Locale.ENGLISH);
        return containsIgnoreCase(item.getUsername(), query)
                || containsIgnoreCase(item.getFullName(), query)
                || containsIgnoreCase(item.getHeadline(), query)
                || containsIgnoreCase(item.getPreferredTemplate(), query);
    }

    private boolean matchesSubscriptionQuery(Subscription subscription, String q) {
        if (!hasText(q)) {
            return true;
        }
        String query = q.trim().toLowerCase(Locale.ENGLISH);
        return containsIgnoreCase(subscription.getUser() != null ? subscription.getUser().getUsername() : null, query)
                || containsIgnoreCase(subscription.getUser() != null ? subscription.getUser().getEmail() : null, query)
                || containsIgnoreCase(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().name() : null, query);
    }

    private <E extends Enum<E>> boolean matchesEnumName(E value, String expected) {
        if (!hasText(expected)) {
            return true;
        }
        return value != null && value.name().equalsIgnoreCase(expected.trim());
    }

    private String profileFirstName(User user) {
        return user != null && user.getProfile() != null ? user.getProfile().getFirstName() : null;
    }

    private String profileLastName(User user) {
        return user != null && user.getProfile() != null ? user.getProfile().getLastName() : null;
    }

    private String fullName(User user) {
        String firstName = profileFirstName(user);
        String lastName = profileLastName(user);
        String joined = Stream.of(firstName, lastName)
                .filter(this::hasText)
                .collect(Collectors.joining(" "));
        return hasText(joined) ? joined : null;
    }

    private boolean sameUserId(User user, Long userId) {
        return user != null && Objects.equals(user.getId(), userId);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(query);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Instant toInstant(LocalDateTime value) {
        return value != null ? value.toInstant(ZoneOffset.UTC) : null;
    }

    private <T> long countByInstantMonth(Collection<T> items, YearMonth month, Function<T, Instant> extractor) {
        return items.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(value -> YearMonth.from(value.atZone(ZoneOffset.UTC)))
                .filter(month::equals)
                .count();
    }

    private <T> long countByLocalDateTimeMonth(Collection<T> items, YearMonth month, Function<T, LocalDateTime> extractor) {
        return items.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(YearMonth::from)
                .filter(month::equals)
                .count();
    }

    private <T> AdminPageResponse<T> page(List<T> items, Integer requestedPage, Integer requestedSize) {
        int page = requestedPage != null && requestedPage >= 0 ? requestedPage : 0;
        int size = requestedSize != null && requestedSize > 0 ? Math.min(requestedSize, 50) : 10;
        int total = items.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);

        return AdminPageResponse.<T>builder()
                .items(items.subList(from, to))
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .build();
    }
}
