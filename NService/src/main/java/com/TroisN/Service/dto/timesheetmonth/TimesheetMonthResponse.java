package com.TroisN.Service.dto.timesheetmonth;

import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class TimesheetMonthResponse {

    private Long id;

    private Long candidateId;
    private String candidateFirstName;
    private String candidateLastName;
    private String candidateEmail;

    private Integer month;
    private Integer year;

    // D-Entrée
    private LocalDate entryDate;

    // Période de congé (intervalle)
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;

    // Repos récurrents (chaque semaine)
    private Set<DayOfWeek> weeklyRestDays;

    // Repos spécifiques (jours variables)
    private Set<LocalDate> specificRestDates;

    private Integer daysInMonth;
    private Integer daysWorked;
    private Integer absenceDays;
    private Integer paidLeaveDays;

    private BigDecimal travelFees;
    private BigDecimal salaryReminder;
    private BigDecimal salaryAdvance;
    private BigDecimal kmIndemnity;

    private String cityAssignment;
    private String remarks;

    private LocalDateTime createdAt;
}