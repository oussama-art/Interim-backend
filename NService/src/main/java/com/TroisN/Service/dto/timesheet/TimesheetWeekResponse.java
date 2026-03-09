package com.TroisN.Service.dto.timesheet;

import java.time.LocalDate;
import java.util.List;

public record TimesheetWeekResponse(
        Long contractId,
        Long demandeId,
        String demandeReference,
        Long candidateId,
        String candidateFullName,
        LocalDate weekStart,
        LocalDate weekEnd,
        double totalHours,
        List<TimesheetDayResponse> days
) {}