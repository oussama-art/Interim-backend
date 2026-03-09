package com.TroisN.Service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(
        name = "timesheet_days",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timesheet_contract_date",
                        columnNames = {"contract_id", "day_date"}
                )
        },
        indexes = {
                @Index(name = "idx_timesheet_contract_date", columnList = "contract_id, day_date")
        }
)
public class TimesheetDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "day_date", nullable = false)
    private LocalDate date;

    @Column(name = "is_rest_day", nullable = false)
    private boolean restDay;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "hours")
    private Double hours;
}