package com.TroisN.Service.dto.timesheet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TimesheetPeriodTotalRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Min(0) double totalHours,
        Integer defaultBreakMinutes
) {}