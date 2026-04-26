package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.naumen.bpms.model.report.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}