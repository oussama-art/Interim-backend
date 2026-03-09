package com.TroisN.Service.controller;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.dto.offer.OfferAcceptRequest;
import com.TroisN.Service.dto.offer.OfferCreateRequest;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.dto.offer.offreCandidate.OfferAddCandidatesRequest;
import com.TroisN.Service.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/offers")
    public ResponseEntity<List<OfferResponse>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }




    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{clientId}/offers")  // ← Ajouté /{clientId}/offers
    public ResponseEntity<OfferResponse> createOffer(
            @PathVariable Long clientId,
            @Valid @RequestBody OfferCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offerService.createOffer(clientId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{clientId}/offers/{offerId}/candidates")
    public ResponseEntity<OfferResponse> addCandidatesToOffer(
            @PathVariable Long clientId,
            @PathVariable Long offerId,
            @Valid @RequestBody OfferAddCandidatesRequest request
    ) {
        return ResponseEntity.ok(
                offerService.addCandidatesToOffer(offerId, clientId, request)
        );
    }


    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{clientId}/offers/{offerId}/accept")
    public ResponseEntity<AssignmentResponse> acceptOffer(
            @PathVariable Long clientId,
            @PathVariable Long offerId,
            @Valid @RequestBody OfferAcceptRequest request
    ) {
        AssignmentResponse response = offerService.acceptOffer(
                offerId,
                request.getCandidateId(),
                clientId,
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable Long offerId
    ) {
        return ResponseEntity.ok(
                offerService.getOfferById(offerId)
        );
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{clientId}/offers")
    public ResponseEntity<List<OfferResponse>> getOffersByClient(
            @PathVariable Long clientId
    ) {
        return ResponseEntity.ok(
                offerService.getOffersByClientId(clientId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/offers/{offerId}")
    public ResponseEntity<Void> deleteOfferByAdmin(
            @PathVariable Long offerId
    ) {
        offerService.deleteOfferByAdmin(offerId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{clientId}/offers/{offerId}/reject/{candidateId}")
    public ResponseEntity<Void> rejectCandidate(
            @PathVariable Long clientId,
            @PathVariable Long offerId,
            @PathVariable Long candidateId
    ) {
        offerService.rejectCandidate(
                offerId,
                candidateId,
                clientId
        );

        return ResponseEntity.noContent().build();
    }




}
