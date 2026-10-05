package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {

    List<WasteReport> findByReportedBy_UserIdOrderByReportedAtDesc(Long userId);

}
