package com.TroisN.Service.repository;

import com.TroisN.Service.entity.TimesheetMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimesheetMonthRepository extends JpaRepository<TimesheetMonth, Long> {

    Optional<TimesheetMonth> findByCandidateIdAndMonthAndYear(Long candidateId, Integer month, Integer year);

    boolean existsByCandidateIdAndMonthAndYear(Long candidateId, Integer month, Integer year);

    List<TimesheetMonth> findAllByCandidateIdOrderByYearDescMonthDesc(Long candidateId);

    Page<TimesheetMonth> findAllByCandidateId(Long candidateId, Pageable pageable);
}