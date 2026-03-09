package com.TroisN.Service.service;

import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthCreateRequest;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthResponse;
import com.TroisN.Service.dto.timesheetmonth.TimesheetMonthUpdateRequest;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.entity.TimesheetMonth;
import com.TroisN.Service.mapper.TimesheetMonthMapper;
import com.TroisN.Service.repository.CandidateRepository;
import com.TroisN.Service.repository.TimesheetMonthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimesheetMonthService {

    private final TimesheetMonthRepository timesheetMonthRepository;
    private final CandidateRepository candidateRepository;
    private final TimesheetMonthMapper mapper;


    public TimesheetMonthResponse create(TimesheetMonthCreateRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));

        if (timesheetMonthRepository.existsByCandidateIdAndMonthAndYear(
                candidate.getId(), request.getMonth(), request.getYear())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "TimesheetMonth already exists for this candidate/month/year"
            );
        }

        TimesheetMonth entity = mapper.toEntity(request, candidate);

        try {
            TimesheetMonth saved = timesheetMonthRepository.save(entity);
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // cas concurrence: contrainte unique déclenchée
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate timesheet for candidate/month/year",
                    ex
            );
        }
    }


    @Transactional(readOnly = true)
    public TimesheetMonthResponse getById(Long id) {
        TimesheetMonth entity = timesheetMonthRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TimesheetMonth not found"));
        return mapper.toResponse(entity);
    }

    // LIST by Candidate (sans pagination)
    @Transactional(readOnly = true)
    public List<TimesheetMonthResponse> listByCandidate(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }

        return timesheetMonthRepository.findAllByCandidateIdOrderByYearDescMonthDesc(candidateId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // PAGE by Candidate (pagination)
    @Transactional(readOnly = true)
    public Page<TimesheetMonthResponse> pageByCandidate(Long candidateId, Pageable pageable) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }

        return timesheetMonthRepository.findAllByCandidateId(candidateId, pageable)
                .map(mapper::toResponse);
    }


    public TimesheetMonthResponse update(Long id, TimesheetMonthUpdateRequest request) {
        TimesheetMonth entity = timesheetMonthRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TimesheetMonth not found"));

        Long candidateId = entity.getCandidate().getId();

        boolean willChangeKey =
                !entity.getMonth().equals(request.getMonth()) ||
                        !entity.getYear().equals(request.getYear());

        if (willChangeKey && timesheetMonthRepository.existsByCandidateIdAndMonthAndYear(
                candidateId, request.getMonth(), request.getYear())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another TimesheetMonth already exists for this candidate/month/year"
            );
        }

        mapper.updateEntity(entity, request);

        try {
            TimesheetMonth saved = timesheetMonthRepository.save(entity);
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate timesheet for candidate/month/year",
                    ex
            );
        }
    }


    public void delete(Long id) {
        TimesheetMonth entity = timesheetMonthRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TimesheetMonth not found"));
        timesheetMonthRepository.delete(entity);
    }
}