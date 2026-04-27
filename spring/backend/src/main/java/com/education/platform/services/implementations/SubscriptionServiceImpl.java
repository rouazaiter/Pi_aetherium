package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.subscription.SubscriptionRequest;
import com.education.platform.dto.subscription.SubscriptionPlanResponse;
import com.education.platform.dto.subscription.SubscriptionResponse;
import com.education.platform.entities.SubscriptionPlan;
import com.education.platform.entities.SubscriptionStatus;
import com.education.platform.entities.Subscription;
import com.education.platform.entities.User;
import com.education.platform.repositories.SubscriptionRepository;
import com.education.platform.services.interfaces.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Map<SubscriptionPlan, PlanDefinition> PLAN_DEFINITIONS = Map.of(
            SubscriptionPlan.STANDARD, new PlanDefinition(
                    24.90,
                    30,
                    0,
                    List.of(
                            "Session securisee avec enregistrement en cas de probleme",
                            "Exercices pratiques pour mieux comprendre",
                            "Supervision apres la session et contact avec un expert"
                    )
            ),
            SubscriptionPlan.PREMIUM, new PlanDefinition(
                    49.90,
                    30,
                    0,
                    List.of(
                            "Tout ce qui est inclus dans Standard",
                            "Telechargement de la session pour la revoir a tout moment"
                    )
            )
    );

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionPlanResponse> listAvailablePlans() {
        return PLAN_DEFINITIONS.entrySet().stream()
                .sorted((a, b) -> Integer.compare(a.getKey().ordinal(), b.getKey().ordinal()))
                .map(entry -> SubscriptionPlanResponse.builder()
                        .plan(entry.getKey())
                        .monthlyPrice(entry.getValue().monthlyPrice())
                        .durationDays(entry.getValue().durationDays())
                        .trialDays(entry.getValue().trialDays())
                        .features(entry.getValue().features())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse createForUser(User user, SubscriptionRequest request) {
        LocalDate start = request.getDateOfSubscription() != null ? request.getDateOfSubscription() : LocalDate.now();
        LocalDate billing = request.getBillingDate() != null ? request.getBillingDate() : start;
        LocalDate expiration = request.getExpirationDate() != null
                ? request.getExpirationDate()
                : start.plusDays(resolvePlanDefinition(request.getSubscriptionPlan()).durationDays());
        if (expiration.isBefore(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date d'expiration doit être après le début de l'abonnement");
        }

        subscriptionRepository.findFirstByUser_IdAndStatusOrderByDateOfSubscriptionDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .ifPresent(previous -> {
                    previous.setStatus(SubscriptionStatus.CANCELLED);
                    previous.setAutoRenew(false);
                });

        Subscription sub = Subscription.builder()
                .user(user)
                .dateOfSubscription(start)
                .subscriptionPlan(request.getSubscriptionPlan())
                .status(SubscriptionStatus.ACTIVE)
                .expirationDate(expiration)
                .billingDate(billing)
                .autoRenew(request.getAutoRenew() == null || request.getAutoRenew())
                .build();
        subscriptionRepository.save(sub);
        return toResponse(sub);
    }

    @Override
    public List<SubscriptionResponse> listForUser(User user) {
        return subscriptionRepository.findByUser_IdOrderByDateOfSubscriptionDesc(user.getId()).stream()
                .map(this::markAsExpiredIfNeeded)
                .map(SubscriptionServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse getCurrentForUser(User user) {
        Subscription subscription = subscriptionRepository
                .findFirstByUser_IdAndStatusOrderByDateOfSubscriptionDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aucun abonnement actif"));
        subscription = markAsExpiredIfNeeded(subscription);
        if (effectiveStatus(subscription) != SubscriptionStatus.ACTIVE) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Aucun abonnement actif");
        }
        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelForUser(User user, Long subscriptionId) {
        Subscription subscription = findOwnedSubscription(user, subscriptionId);
        if (effectiveStatus(subscription) == SubscriptionStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cet abonnement est deja annule");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateAutoRenew(User user, Long subscriptionId, boolean autoRenew) {
        Subscription subscription = findOwnedSubscription(user, subscriptionId);
        if (effectiveStatus(subscription) != SubscriptionStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Seul un abonnement actif peut etre modifie");
        }
        subscription.setAutoRenew(autoRenew);
        return toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoiceForUser(User user, Long subscriptionId) {
        Subscription subscription = findOwnedSubscription(user, subscriptionId);
        PlanDefinition definition = resolvePlanDefinition(subscription.getSubscriptionPlan());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String issuedAt = LocalDate.now().format(dateFormatter);
        String billingDate = subscription.getBillingDate().format(dateFormatter);
        String startDate = subscription.getDateOfSubscription().format(dateFormatter);
        String planName = subscription.getSubscriptionPlan() == SubscriptionPlan.PREMIUM ? "Business Pro" : "Free Tier";
        String status = effectiveStatus(subscription).name();
        String statusClass = "ACTIVE".equals(status) ? "paid" : "pending";
        double subtotal = definition.monthlyPrice();
        double addOn = subscription.getSubscriptionPlan() == SubscriptionPlan.PREMIUM ? 10.00 : 0.00;
        double total = subtotal + addOn;
        String fullName = (user.getProfile() != null
                ? ((user.getProfile().getFirstName() == null ? "" : user.getProfile().getFirstName()) + " "
                + (user.getProfile().getLastName() == null ? "" : user.getProfile().getLastName())).trim()
                : "");
        if (fullName.isBlank()) {
            fullName = user.getUsername();
        }

        String htmlTemplate = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>SkillHub Invoice</title>
                  <style>
                    body { font-family: Inter, Segoe UI, Arial, sans-serif; background:#f6f7fb; margin:0; padding:24px; color:#1f2937; }
                    .invoice { max-width:760px; margin:0 auto; background:#fff; border:1px solid #e5e7eb; border-radius:16px; padding:26px; box-shadow:0 10px 34px rgba(79,70,229,.08); }
                    .head { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
                    .brand { display:flex; gap:12px; align-items:center; }
                    .logo { width:34px; height:34px; border-radius:8px; background:linear-gradient(135deg,#4f46e5,#7c3aed); color:#fff; display:grid; place-items:center; font-weight:800; }
                    h1 { margin:0; font-size:23px; letter-spacing:-.02em; }
                    .sub { margin:2px 0 0; color:#6b7280; font-size:13px; }
                    .badge { border-radius:999px; padding:6px 10px; font-size:12px; font-weight:700; }
                    .badge.paid { background:#dcfce7; color:#166534; }
                    .badge.pending { background:#ffedd5; color:#9a3412; }
                    .meta { margin-top:18px; display:grid; grid-template-columns:1fr 1fr; gap:18px; padding-top:16px; border-top:1px solid #eef0f6; }
                    .meta h4 { margin:0 0 8px; color:#9ca3af; font-size:11px; text-transform:uppercase; letter-spacing:.08em; }
                    .meta p { margin:0; line-height:1.45; font-size:13px; }
                    table { width:100%; border-collapse:collapse; margin-top:20px; }
                    th, td { border-top:1px solid #eef0f6; padding:11px 8px; text-align:left; font-size:13px; }
                    th { font-size:11px; color:#9ca3af; letter-spacing:.08em; text-transform:uppercase; }
                    .totals { margin-left:auto; width:260px; margin-top:18px; }
                    .totals .row { display:flex; justify-content:space-between; padding:6px 0; color:#6b7280; font-size:13px; }
                    .totals .final { border-top:1px solid #e5e7eb; margin-top:4px; padding-top:10px; font-size:20px; font-weight:800; color:#4f46e5; }
                    .foot { margin-top:18px; border-top:1px solid #eef0f6; padding-top:12px; font-size:12px; color:#6b7280; display:flex; justify-content:space-between; gap:12px; }
                  </style>
                </head>
                <body>
                  <article class="invoice">
                    <header class="head">
                      <div>
                        <div class="brand">
                          <div class="logo">S</div>
                          <div>
                            <h1>SkillHub Subscription Invoice</h1>
                            <p class="sub">INV-__INVOICE_ID__ · Issued on __ISSUED_AT__</p>
                          </div>
                        </div>
                      </div>
                      <span class="badge __STATUS_CLASS__">__STATUS__</span>
                    </header>

                    <section class="meta">
                      <div>
                        <h4>From</h4>
                        <p><strong>SkillHub Learning Platform</strong><br/>Remote Office, Digital Campus<br/>support@skillhub.com</p>
                      </div>
                      <div>
                        <h4>To</h4>
                        <p><strong>__FULL_NAME__</strong><br/>__EMAIL__</p>
                      </div>
                    </section>

                    <table>
                      <thead>
                        <tr>
                          <th>Description</th>
                          <th>Qty</th>
                          <th>Price</th>
                          <th>Total</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>__PLAN_NAME__ Monthly<br/><span style="color:#9ca3af;font-size:12px;">Plan starts on __START_DATE__ · Next billing __BILLING_DATE__</span></td>
                          <td>1</td>
                          <td>$__PLAN_PRICE__</td>
                          <td>$__PLAN_TOTAL__</td>
                        </tr>
                        <tr>
                          <td>AI Add-on<br/><span style="color:#9ca3af;font-size:12px;">Enhanced learning insights and automation</span></td>
                          <td>1</td>
                          <td>$__ADDON_PRICE__</td>
                          <td>$__ADDON_TOTAL__</td>
                        </tr>
                      </tbody>
                    </table>

                    <section class="totals">
                      <div class="row"><span>Subtotal</span><strong>$__SUBTOTAL__</strong></div>
                      <div class="row"><span>Tax (0%%)</span><strong>$0.00</strong></div>
                      <div class="row final"><span>Total</span><span>$__TOTAL__</span></div>
                    </section>

                    <footer class="foot">
                      <span>Payment method: Auto-renew __AUTORENEW__</span>
                      <span>Need help? support@skillhub.com</span>
                    </footer>
                  </article>
                </body>
                </html>
                """;
        String html = htmlTemplate
                .replace("__INVOICE_ID__", String.valueOf(subscription.getId()))
                .replace("__ISSUED_AT__", issuedAt)
                .replace("__STATUS_CLASS__", statusClass)
                .replace("__STATUS__", status)
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__EMAIL__", escapeHtml(user.getEmail()))
                .replace("__PLAN_NAME__", planName)
                .replace("__START_DATE__", startDate)
                .replace("__BILLING_DATE__", billingDate)
                .replace("__PLAN_PRICE__", String.format("%.2f", definition.monthlyPrice()))
                .replace("__PLAN_TOTAL__", String.format("%.2f", definition.monthlyPrice()))
                .replace("__ADDON_PRICE__", String.format("%.2f", addOn))
                .replace("__ADDON_TOTAL__", String.format("%.2f", addOn))
                .replace("__SUBTOTAL__", String.format("%.2f", subtotal + addOn))
                .replace("__TOTAL__", String.format("%.2f", total))
                .replace("__AUTORENEW__", Boolean.TRUE.equals(subscription.getAutoRenew()) ? "enabled" : "disabled");
        return html.getBytes(StandardCharsets.UTF_8);
    }

    private Subscription findOwnedSubscription(User user, Long subscriptionId) {
        return subscriptionRepository.findByIdAndUser_Id(subscriptionId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Abonnement introuvable"));
    }

    private Subscription markAsExpiredIfNeeded(Subscription subscription) {
        if (effectiveStatus(subscription) == SubscriptionStatus.ACTIVE && subscription.getExpirationDate().isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setAutoRenew(false);
        }
        return subscription;
    }

    private SubscriptionStatus effectiveStatus(Subscription subscription) {
        return subscription.getStatus() == null ? SubscriptionStatus.ACTIVE : subscription.getStatus();
    }

    private PlanDefinition resolvePlanDefinition(SubscriptionPlan plan) {
        PlanDefinition definition = PLAN_DEFINITIONS.get(plan);
        if (definition == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plan d'abonnement non supporte");
        }
        return definition;
    }

    private static SubscriptionResponse toResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .dateOfSubscription(s.getDateOfSubscription())
                .subscriptionPlan(s.getSubscriptionPlan())
                .status(s.getStatus() == null ? SubscriptionStatus.ACTIVE : s.getStatus())
                .expirationDate(s.getExpirationDate())
                .billingDate(s.getBillingDate())
                .autoRenew(Boolean.TRUE.equals(s.getAutoRenew()))
                .build();
    }

    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record PlanDefinition(double monthlyPrice, int durationDays, int trialDays, List<String> features) {
    }
}
