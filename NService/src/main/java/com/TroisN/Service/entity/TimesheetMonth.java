package com.TroisN.Service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(
        name = "timesheet_months",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_timesheet_candidate_month_year",
                columnNames = {"candidate_id", "month", "year"}
        )
)
public class TimesheetMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // D-Entrée
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    // Période de congé (intervalle)
    @Column(name = "leave_start_date")
    private LocalDate leaveStartDate;

    @Column(name = "leave_end_date")
    private LocalDate leaveEndDate;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "timesheet_month_weekly_rest_days",
            joinColumns = @JoinColumn(name = "timesheet_month_id")
    )
    @Column(name = "day_of_week", nullable = false, length = 10)
    @Enumerated(EnumType.STRING) // important: stocker "MONDAY" au lieu d'un numéro
    private Set<DayOfWeek> weeklyRestDays = new HashSet<>();

    // Dates de repos spécifiques (jours variables)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "timesheet_month_specific_rest_dates",
            joinColumns = @JoinColumn(name = "timesheet_month_id")
    )
    @Column(name = "rest_date", nullable = false)
    private Set<LocalDate> specificRestDates = new HashSet<>();

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer daysInMonth;

    @Column(nullable = false)
    private Integer daysWorked;

    @Column(nullable = false)
    private Integer absenceDays;

    @Column(nullable = false)
    private Integer paidLeaveDays;

    @Column(precision = 12, scale = 2)
    private BigDecimal travelFees;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryReminder;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryAdvance;

    @Column(precision = 12, scale = 2)
    private BigDecimal kmIndemnity;

    private String cityAssignment;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (absenceDays == null) absenceDays = 0;
        if (paidLeaveDays == null) paidLeaveDays = 0;
        if (daysWorked == null) daysWorked = 0;
        if (daysInMonth == null) daysInMonth = 0;
    }
}