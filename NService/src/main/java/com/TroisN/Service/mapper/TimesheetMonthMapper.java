package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthCreateRequest;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthResponse;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthUpdateRequest;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.entity.TimesheetMonth;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class TimesheetMonthMapper {

    public TimesheetMonth toEntity(TimesheetMonthCreateRequest req, Candidate candidate) {
        TimesheetMonth e = new TimesheetMonth();
        e.setCandidate(candidate);

        // Nouveaux champs (form + excel)
        e.setEntryDate(req.getEntryDate());
        e.setLeaveStartDate(req.getLeaveStartDate());
        e.setLeaveEndDate(req.getLeaveEndDate());

        e.setWeeklyRestDays(req.getWeeklyRestDays() == null ? new HashSet<>() : new HashSet<>(req.getWeeklyRestDays()));
        e.setSpecificRestDates(req.getSpecificRestDates() == null ? new HashSet<>() : new HashSet<>(req.getSpecificRestDates()));

        // Clé mois/année
        e.setMonth(req.getMonth());
        e.setYear(req.getYear());

        // Champs Excel
        e.setDaysInMonth(req.getDaysInMonth());
        e.setDaysWorked(req.getDaysWorked());
        e.setAbsenceDays(req.getAbsenceDays());
        e.setPaidLeaveDays(req.getPaidLeaveDays());

        e.setTravelFees(req.getTravelFees());
        e.setSalaryReminder(req.getSalaryReminder());
        e.setSalaryAdvance(req.getSalaryAdvance());
        e.setKmIndemnity(req.getKmIndemnity());

        e.setCityAssignment(req.getCityAssignment());
        e.setRemarks(req.getRemarks());

        return e;
    }

    public void updateEntity(TimesheetMonth e, TimesheetMonthUpdateRequest req) {
        // Nouveaux champs
        e.setEntryDate(req.getEntryDate());
        e.setLeaveStartDate(req.getLeaveStartDate());
        e.setLeaveEndDate(req.getLeaveEndDate());

        e.setWeeklyRestDays(req.getWeeklyRestDays() == null ? new HashSet<>() : new HashSet<>(req.getWeeklyRestDays()));
        e.setSpecificRestDates(req.getSpecificRestDates() == null ? new HashSet<>() : new HashSet<>(req.getSpecificRestDates()));

        // Clé mois/année
        e.setMonth(req.getMonth());
        e.setYear(req.getYear());

        // Champs Excel
        e.setDaysInMonth(req.getDaysInMonth());
        e.setDaysWorked(req.getDaysWorked());
        e.setAbsenceDays(req.getAbsenceDays());
        e.setPaidLeaveDays(req.getPaidLeaveDays());

        e.setTravelFees(req.getTravelFees());
        e.setSalaryReminder(req.getSalaryReminder());
        e.setSalaryAdvance(req.getSalaryAdvance());
        e.setKmIndemnity(req.getKmIndemnity());

        e.setCityAssignment(req.getCityAssignment());
        e.setRemarks(req.getRemarks());
    }

    public TimesheetMonthResponse toResponse(TimesheetMonth e) {
        TimesheetMonthResponse r = new TimesheetMonthResponse();

        r.setId(e.getId());

        // Nouveaux champs
        r.setEntryDate(e.getEntryDate());
        r.setLeaveStartDate(e.getLeaveStartDate());
        r.setLeaveEndDate(e.getLeaveEndDate());

        r.setWeeklyRestDays(e.getWeeklyRestDays() == null ? new HashSet<>() : new HashSet<>(e.getWeeklyRestDays()));
        r.setSpecificRestDates(e.getSpecificRestDates() == null ? new HashSet<>() : new HashSet<>(e.getSpecificRestDates()));

        // Clé mois/année
        r.setMonth(e.getMonth());
        r.setYear(e.getYear());

        // Champs Excel
        r.setDaysInMonth(e.getDaysInMonth());
        r.setDaysWorked(e.getDaysWorked());
        r.setAbsenceDays(e.getAbsenceDays());
        r.setPaidLeaveDays(e.getPaidLeaveDays());

        r.setTravelFees(e.getTravelFees());
        r.setSalaryReminder(e.getSalaryReminder());
        r.setSalaryAdvance(e.getSalaryAdvance());
        r.setKmIndemnity(e.getKmIndemnity());

        r.setCityAssignment(e.getCityAssignment());
        r.setRemarks(e.getRemarks());

        r.setCreatedAt(e.getCreatedAt());

        if (e.getCandidate() != null) {
            r.setCandidateId(e.getCandidate().getId());
            r.setCandidateFirstName(e.getCandidate().getFirstName());
            r.setCandidateLastName(e.getCandidate().getLastName());
            r.setCandidateEmail(e.getCandidate().getEmailAddress());
        }

        return r;
    }
}