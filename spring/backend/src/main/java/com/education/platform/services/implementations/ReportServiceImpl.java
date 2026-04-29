package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.education.platform.entities.blog.Report;
import com.education.platform.entities.user.User;
import com.education.platform.repositories.blog.ReportRepository;
import com.education.platform.repositories.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReportServiceImpl {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public Report submitReport(Long reporterId, Report.TargetType targetType,
                               Long targetId, Report.ReportReason reason, String details) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("User not found: " + reporterId));

        // Prevent duplicate reports from same user on same target
        reportRepository.findByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)
                .ifPresent(r -> { throw new RuntimeException("You have already reported this content"); });

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDetails(details);
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.PENDING);
    }

    public Report updateStatus(Long reportId, Report.ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
        report.setStatus(status);
        report.setReviewedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
}
