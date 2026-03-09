package com.TroisN.Service.controller;

import com.TroisN.Service.dto.timesheet.TimesheetDayResponse;
import com.TroisN.Service.dto.timesheet.TimesheetDayUserRequest;
import com.TroisN.Service.dto.timesheet.TimesheetPeriodTotalRequest;
import com.TroisN.Service.service.UserTimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timesheets")
@PreAuthorize("hasRole('CLIENT')")
public class UserTimesheetController {

    private final UserTimesheetService userTimesheetService;

    // ✅ Créer / Modifier un jour (Upsert)
    // PUT /api/timesheets/contracts/3/days/2026-03-02?candidateId=15
    @PutMapping("/contracts/{contractId}/days/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertDay(
            @PathVariable Long contractId,
            @PathVariable LocalDate date,
            @RequestParam Long candidateId,
            @Valid @RequestBody TimesheetDayUserRequest request,
            JwtAuthenticationToken auth
    ) {
        requireCandidateId(candidateId);
        String clientSub = auth.getToken().getSubject();
        userTimesheetService.upsertDayForClient(contractId, candidateId, date, request, clientSub);
    }

    // ✅ Modifier partiellement (PATCH)
    // PATCH /api/timesheets/contracts/3/days/2026-03-02?candidateId=15
    @PatchMapping("/contracts/{contractId}/days/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchDay(
            @PathVariable Long contractId,
            @PathVariable LocalDate date,
            @RequestParam Long candidateId,
            @RequestBody TimesheetDayUserRequest request,
            JwtAuthenticationToken auth
    ) {
        requireCandidateId(candidateId);
        String clientSub = auth.getToken().getSubject();
        userTimesheetService.patchDayForClient(contractId, candidateId, date, request, clientSub);
    }

    // ✅ Supprimer un jour travaillé
    // DELETE /api/timesheets/contracts/3/days/2026-03-02?candidateId=15
    @DeleteMapping("/contracts/{contractId}/days/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDay(
            @PathVariable Long contractId,
            @PathVariable LocalDate date,
            @RequestParam Long candidateId,
            JwtAuthenticationToken auth
    ) {
        requireCandidateId(candidateId);
        String clientSub = auth.getToken().getSubject();
        userTimesheetService.deleteDayForClient(contractId, candidateId, date, clientSub);
    }

    // ✅ Saisie total sur une période
    // POST /api/timesheets/contracts/3/period-total?candidateId=15
    @PostMapping("/contracts/{contractId}/period-total")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitPeriodTotal(
            @PathVariable Long contractId,
            @RequestParam Long candidateId,
            @Valid @RequestBody TimesheetPeriodTotalRequest request,
            JwtAuthenticationToken auth
    ) {
        requireCandidateId(candidateId);
        String clientSub = auth.getToken().getSubject();
        userTimesheetService.submitTotalForPeriodForClient(contractId, candidateId, request, clientSub);
    }

    // ✅ Récupérer les jours travaillés
    @GetMapping("/contracts/{contractId}/worked-days")
    public List<TimesheetDayResponse> getWorkedDays(
            @PathVariable Long contractId,
            @RequestParam Long candidateId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            JwtAuthenticationToken auth
    ) {
        requireCandidateId(candidateId);
        String clientSub = auth.getToken().getSubject();
        return userTimesheetService.getWorkedDaysForClient(contractId, candidateId, from, to, clientSub);
    }

    private void requireCandidateId(Long candidateId) {
        if (candidateId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidateId est obligatoire");
        }
    }
}