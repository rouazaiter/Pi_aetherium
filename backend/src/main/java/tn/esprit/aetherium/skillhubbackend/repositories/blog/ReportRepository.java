package tn.esprit.aetherium.skillhubbackend.repositories.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status);
    List<Report> findAllByOrderByCreatedAtDesc();
    Optional<Report> findByReporterIdAndTargetTypeAndTargetId(
        Long reporterId, Report.TargetType targetType, Long targetId);
}
