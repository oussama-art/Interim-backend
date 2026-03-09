package com.TroisN.Service.service;

import com.TroisN.Service.dto.timesheet.*;
import com.TroisN.Service.entity.Contract;
import com.TroisN.Service.entity.TimesheetDay;
import com.TroisN.Service.repository.ContractRepository;
import com.TroisN.Service.repository.TimesheetDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final ContractRepository contractRepository;
    private final TimesheetDayRepository timesheetDayRepository;

    @Transactional(readOnly = true)
    public TimesheetWeekResponse getWeek(Long contractId, LocalDate weekStart) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable avec ID = " + contractId));

        LocalDate start = weekStart.with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        List<TimesheetDay> existing = timesheetDayRepository
                .findByContractIdAndDateBetweenOrderByDateAsc(contractId, start, end);

        List<TimesheetDayResponse> days = new ArrayList<>();
        double total = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);

            TimesheetDay entity = existing.stream()
                    .filter(x -> x.getDate().equals(d))
                    .findFirst()
                    .orElse(null);

            boolean rest = (entity != null)
                    ? entity.isRestDay()
                    : (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY);

            Double hours = (entity != null) ? entity.getHours() : (rest ? 0.0 : null);
            if (hours != null) total += hours;

            String dayLabel = capitalize(d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH));

            days.add(new TimesheetDayResponse(
                    d,
                    dayLabel,
                    rest,
                    hours,
                    entity != null && entity.getStartTime() != null ? entity.getStartTime().toString() : null,
                    entity != null && entity.getEndTime() != null ? entity.getEndTime().toString() : null,
                    entity != null ? entity.getBreakMinutes() : null,
                    entity != null ? entity.getNote() : null
            ));
        }

        String fullName = safe(contract.getCandidate().getFirstName()) + " " + safe(contract.getCandidate().getLastName());
        fullName = fullName.trim().isEmpty() ? ("Candidate #" + contract.getCandidate().getId()) : fullName.trim();

        return new TimesheetWeekResponse(
                contract.getId(),
                contract.getDemande().getId(),
                contract.getDemande().getReference(),
                contract.getCandidate().getId(),
                fullName,
                start,
                end,
                total,
                days
        );
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}