package com.TroisN.Service.dto.timesheetmonth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class TimesheetMonthUpdateRequest {

    @NotNull
    private LocalDate entryDate;

    // Période de congé (intervalle)
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;

    // Repos récurrents
    private Set<DayOfWeek> weeklyRestDays = new HashSet<>();

    // Repos spécifiques
    private Set<LocalDate> specificRestDates = new HashSet<>();

    @NotNull
    @Min(1) @Max(12)
    private Integer month;

    @NotNull
    @Min(2000) @Max(2100)
    private Integer year;

    @NotNull
    @Min(0)
    private Integer daysInMonth;

    @NotNull
    @Min(0)
    private Integer daysWorked;

    @NotNull
    @Min(0)
    private Integer absenceDays;

    @NotNull
    @Min(0)
    private Integer paidLeaveDays;

    @Min(0)
    private BigDecimal travelFees;

    @Min(0)
    private BigDecimal salaryReminder;

    @Min(0)
    private BigDecimal salaryAdvance;

    @Min(0)
    private BigDecimal kmIndemnity;

    private String cityAssignment;

    private String remarks;

    // Validation intervalle congé
    @AssertTrue(message = "leaveStartDate/leaveEndDate doivent être tous les deux null ou tous les deux remplis, et leaveStartDate <= leaveEndDate")
    public boolean isLeavePeriodValid() {
        if (leaveStartDate == null && leaveEndDate == null) return true;
        if (leaveStartDate == null || leaveEndDate == null) return false;
        return !leaveStartDate.isAfter(leaveEndDate);
    }

    // Validation: les dates spécifiques doivent appartenir au month/year
    @AssertTrue(message = "Chaque date dans specificRestDates doit appartenir au month/year du timesheet")
    public boolean isSpecificRestDatesInMonth() {
        if (specificRestDates == null || specificRestDates.isEmpty()) return true;
        if (month == null || year == null) return false;

        for (LocalDate d : specificRestDates) {
            if (d == null) return false;
            if (d.getYear() != year) return false;
            if (d.getMonthValue() != month) return false;
        }
        return true;
    }
}