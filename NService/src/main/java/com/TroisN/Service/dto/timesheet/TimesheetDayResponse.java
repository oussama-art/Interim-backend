package com.TroisN.Service.dto.timesheet;

import java.time.LocalDate;

public record TimesheetDayResponse(
        LocalDate date,
        String dayLabel,
        boolean restDay,
        Double hours,
        String startTime,
        String endTime,
        Integer breakMinutes,
        String note
) {}