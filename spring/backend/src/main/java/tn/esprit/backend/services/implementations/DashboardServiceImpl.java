package tn.esprit.backend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.backend.dto.DashboardCategoryCountDto;
import tn.esprit.backend.dto.DashboardCategoryStatsDto;
import tn.esprit.backend.dto.DashboardEventDto;
import tn.esprit.backend.dto.DashboardEventProjection;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.services.interfaces.DashboardService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Override
    public List<DashboardEventDto> getEvents() {
        return serviceRequestRepository.findDashboardEvents().stream()
                .map(this::toEventDto)
                .toList();
    }

    @Override
    public List<DashboardCategoryStatsDto> getCategoryStats() {
        List<DashboardEventDto> events = getEvents();
        Map<String, CategoryAccumulator> grouped = new LinkedHashMap<>();

        for (DashboardEventDto event : events) {
            String key = event.category();
            CategoryAccumulator acc = grouped.computeIfAbsent(key, ignored -> new CategoryAccumulator());
            acc.totalEvents++;
            acc.totalParticipants += event.participants();
            acc.totalAmount = acc.totalAmount.add(event.amount() == null ? BigDecimal.ZERO : event.amount());
        }

        List<Map.Entry<String, CategoryAccumulator>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER));

        return entries.stream()
                .map(entry -> new DashboardCategoryStatsDto(
                        entry.getKey(),
                        entry.getValue().totalEvents,
                        entry.getValue().totalParticipants,
                        entry.getValue().totalAmount
                ))
                .toList();
        }
// dashboard category count
    @Override
    public List<DashboardCategoryCountDto> getCategoryCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();

        for (ServiceRequest request : serviceRequestRepository.findAll()) {
            String key = request.getCategory() == null ? "Uncategorized" : request.getCategory().name();
            counts.put(key, counts.getOrDefault(key, 0L) + 1L);
        }

        return counts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new DashboardCategoryCountDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private DashboardEventDto toEventDto(DashboardEventProjection projection) {
        return new DashboardEventDto(
                projection.getId(),
                projection.getEventName(),
                projection.getEventDate(),
                projection.getCategory() == null ? null : projection.getCategory().name(),
                projection.getStatus() == null ? null : projection.getStatus().name(),
                projection.getAmount(),
                projection.getParticipants() == null ? 0L : projection.getParticipants()
        );
    }

    private static final class CategoryAccumulator {
        private long totalEvents;
        private long totalParticipants;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }
}
