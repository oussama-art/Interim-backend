package com.TroisN.Service.dto.timesheet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TimesheetDayUserRequest(
        Boolean restDay,

        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                message = "startTime doit être au format HH:mm"
        )
        String startTime,

        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                message = "endTime doit être au format HH:mm"
        )
        String endTime,

        @Min(value = 0, message = "breakMinutes >= 0")
        @Max(value = 24 * 60, message = "breakMinutes trop grand")
        Integer breakMinutes,

        @Min(value = 0, message = "hours >= 0")
        @Max(value = 24, message = "hours <= 24")
        Double hours,

        @Size(max = 200, message = "note max 200 caractères")
        String note
) {}