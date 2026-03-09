package com.TroisN.Service.service;

import com.TroisN.Service.dto.timesheet.TimesheetDayResponse;
import com.TroisN.Service.dto.timesheet.TimesheetDayUserRequest;
import com.TroisN.Service.dto.timesheet.TimesheetPeriodTotalRequest;
import com.TroisN.Service.entity.Contract;
import com.TroisN.Service.entity.TimesheetDay;
import com.TroisN.Service.repository.ClientRepository;
import com.TroisN.Service.repository.ContractRepository;
import com.TroisN.Service.repository.TimesheetDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTimesheetService {

    private final ContractRepository contractRepository;
    private final TimesheetDayRepository timesheetDayRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Long getClientIdFromTokenSub(String sub) {
        return clientRepository.findBykeycloakUserId(sub)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client introuvable pour keycloakUserId(sub) = " + sub
                ))
                .getId();
    }

    // =========================
    // ✅ PUT = Create/Replace day
    // =========================
    @Transactional
    public void upsertDayForClient(Long contractId, Long candidateId, LocalDate date,
                                   TimesheetDayUserRequest req, String clientSub) {

        Long clientId = getClientIdFromTokenSub(clientSub);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        assertClientOwnsContract(contract, clientId);
        assertCandidateMatchesContract(contract, candidateId);
        assertDateInContract(contract, date);

        TimesheetDay day = timesheetDayRepository.findByContractIdAndDate(contractId, date)
                .orElseGet(() -> {
                    TimesheetDay t = new TimesheetDay();
                    t.setContract(contract);
                    t.setDate(date);
                    DayOfWeek dow = date.getDayOfWeek();
                    t.setRestDay(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
                    t.setHours(0.0);
                    return t;
                });

        // PUT = remplace (mais on garde comportement safe: null => pas toucher)
        applyRequest(day, req, true);

        timesheetDayRepository.save(day);
    }

    // =========================
    // ✅ PATCH = Partial update day
    // =========================
    @Transactional
    public void patchDayForClient(Long contractId, Long candidateId, LocalDate date,
                                  TimesheetDayUserRequest req, String clientSub) {

        Long clientId = getClientIdFromTokenSub(clientSub);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        assertClientOwnsContract(contract, clientId);
        assertCandidateMatchesContract(contract, candidateId);
        assertDateInContract(contract, date);

        TimesheetDay day = timesheetDayRepository.findByContractIdAndDate(contractId, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jour introuvable"));

        // PATCH = seulement les champs non null
        applyRequest(day, req, false);

        timesheetDayRepository.save(day);
    }

    // =========================
    // ✅ DELETE day
    // =========================
    @Transactional
    public void deleteDayForClient(Long contractId, Long candidateId, LocalDate date, String clientSub) {

        Long clientId = getClientIdFromTokenSub(clientSub);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        assertClientOwnsContract(contract, clientId);
        assertCandidateMatchesContract(contract, candidateId);
        assertDateInContract(contract, date);

        long deleted = timesheetDayRepository.deleteByContract_IdAndDate(contractId, date);

        // Option pro: si tu veux idempotent, enlève ce if
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jour introuvable (rien à supprimer)");
        }
    }

    // =========================
    // ✅ TOTAL period
    // =========================
    @Transactional
    public void submitTotalForPeriodForClient(Long contractId, Long candidateId,
                                              TimesheetPeriodTotalRequest req, String clientSub) {

        Long clientId = getClientIdFromTokenSub(clientSub);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        assertClientOwnsContract(contract, clientId);
        assertCandidateMatchesContract(contract, candidateId);

        LocalDate start = req.startDate();
        LocalDate end = req.endDate();

        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate et endDate sont obligatoires");
        }
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate doit être >= startDate");
        }
        if (start.isBefore(contract.getStartDate()) || end.isAfter(contract.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Période hors contrat");
        }
        if (req.totalHours() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalHours doit être >= 0");
        }

        // jours ouvrés lun-ven
        List<LocalDate> workingDays = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                workingDays.add(d);
            }
        }

        if (workingDays.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun jour ouvré dans la période");
        }

        double perDay = req.totalHours() / workingDays.size();

        for (LocalDate d : workingDays) {
            TimesheetDay day = timesheetDayRepository.findByContractIdAndDate(contractId, d)
                    .orElseGet(() -> {
                        TimesheetDay t = new TimesheetDay();
                        t.setContract(contract);
                        t.setDate(d);
                        t.setRestDay(false);
                        t.setHours(0.0);
                        return t;
                    });

            day.setRestDay(false);
            if (req.defaultBreakMinutes() != null) {
                day.setBreakMinutes(req.defaultBreakMinutes());
            }
            day.setHours(round2(perDay));

            timesheetDayRepository.save(day);
        }
    }

    // =========================
    // ✅ GET worked days
    // =========================
    @Transactional(readOnly = true)
    public List<TimesheetDayResponse> getWorkedDaysForClient(
            Long contractId,
            Long candidateId,
            LocalDate from,
            LocalDate to,
            String clientSub
    ) {
        Long clientId = getClientIdFromTokenSub(clientSub);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        assertClientOwnsContract(contract, clientId);
        assertCandidateMatchesContract(contract, candidateId);

        LocalDate start = (from != null) ? from : contract.getStartDate();
        LocalDate end = (to != null) ? to : contract.getEndDate();

        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to doit être >= from");
        }
        if (start.isBefore(contract.getStartDate()) || end.isAfter(contract.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Période hors contrat");
        }

        List<TimesheetDay> days = timesheetDayRepository
                .findByContractIdAndDateBetweenAndRestDayFalseOrderByDateAsc(contractId, start, end);

        return days.stream()
                .filter(d -> d.getHours() != null && d.getHours() > 0)
                .map(this::toResponse)
                .toList();
    }

    // =========================
    // Helpers (sécurité + calcul)
    // =========================

    private void assertClientOwnsContract(Contract contract, Long clientId) {
        if (contract.getDemande() == null || contract.getDemande().getClient() == null
                || !contract.getDemande().getClient().getId().equals(clientId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Accès refusé (contrat n'appartient pas à ce client)"
            );
        }
    }

    private void assertCandidateMatchesContract(Contract contract, Long candidateId) {
        if (contract.getCandidate() == null || !contract.getCandidate().getId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidateId ne correspond pas au contrat");
        }
    }

    private void assertDateInContract(Contract contract, LocalDate date) {
        if (date.isBefore(contract.getStartDate()) || date.isAfter(contract.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date hors période du contrat");
        }
    }

    /**
     * @param isPut true => comportement PUT (peut “remplacer” la saisie),
     *              false => PATCH (ne touche que les champs non null)
     */
    private void applyRequest(TimesheetDay day, TimesheetDayUserRequest req, boolean isPut) {

        // PATCH: si req est null on ne fait rien
        if (req == null) return;

        if (req.restDay() != null) day.setRestDay(req.restDay());
        if (req.breakMinutes() != null) day.setBreakMinutes(req.breakMinutes());
        if (req.note() != null) day.setNote(req.note());

        if (req.startTime() != null) day.setStartTime(LocalTime.parse(req.startTime()));
        if (req.endTime() != null) day.setEndTime(LocalTime.parse(req.endTime()));

        // repos => hours=0
        if (day.isRestDay()) {
            day.setHours(0.0);
            return;
        }

        // si hours fourni => priorité
        if (req.hours() != null) {
            day.setHours(round2(req.hours()));
            return;
        }

        // sinon calc si possible
        Double computed = calcHours(day);
        if (computed != null) {
            day.setHours(computed);
        } else if (isPut) {
            // en PUT, si pas de quoi calculer et pas hours => on laisse tel quel
            // (pas d'erreur, car tu peux vouloir juste changer note/break/restDay)
        }
    }

    private Double calcHours(TimesheetDay day) {
        if (day.isRestDay()) return 0.0;
        if (day.getStartTime() == null || day.getEndTime() == null) return null;

        long minutes = Duration.between(day.getStartTime(), day.getEndTime()).toMinutes();
        if (minutes < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime doit être après startTime");
        }

        int pause = (day.getBreakMinutes() != null) ? day.getBreakMinutes() : 0;
        minutes = Math.max(0, minutes - pause);

        return round2(minutes / 60.0);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private TimesheetDayResponse toResponse(TimesheetDay d) {
        return new TimesheetDayResponse(
                d.getDate(),
                dayLabelFr(d.getDate().getDayOfWeek()),
                d.isRestDay(),
                d.getHours(),
                d.getStartTime() != null ? d.getStartTime().toString() : null,
                d.getEndTime() != null ? d.getEndTime().toString() : null,
                d.getBreakMinutes(),
                d.getNote()
        );
    }

    private String dayLabelFr(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }
}