package com.TroisN.Service.repository;

import com.TroisN.Service.entity.TimesheetDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimesheetDayRepository extends JpaRepository<TimesheetDay, Long> {

    List<TimesheetDay> findByContractIdAndDateBetweenOrderByDateAsc(Long contractId, LocalDate start, LocalDate end);

    Optional<TimesheetDay> findByContractIdAndDate(Long contractId, LocalDate date);

    List<TimesheetDay> findByContractIdAndDateBetweenAndRestDayFalseOrderByDateAsc(
            Long contractId,
            LocalDate start,
            LocalDate end
    );



    long deleteByContract_IdAndDate(
            Long contractId,
            LocalDate date
    );


}