package com.TroisN.Service.controller;
import com.TroisN.Service.dto.demande.DemandeRequest;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.service.DemandeService;
import com.TroisN.Service.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final OfferService offerService;


    public DemandeController(DemandeService demandeService,OfferService offerService) {
        this.demandeService = demandeService;
        this.offerService = offerService;
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{demandeId}")
    public DemandeResponse getDemandeById(
            Authentication authentication,
            @PathVariable Long demandeId
    ) {
        return demandeService.getDemandeByIdAndClient(authentication, demandeId);
    }


    @PostMapping("/create")
    public DemandeResponse createDemande(
            Authentication authentication,
            @Valid @RequestBody DemandeRequest request
    ) {
        return demandeService.createDemande(authentication, request);
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PatchMapping("/{demandeId}/close")
    public DemandeResponse closeDemande(
            @PathVariable Long demandeId
    ) {
        return demandeService.closeDemande(demandeId);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public Page<DemandeResponse> getAllDemadandes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return demandeService.getAllDemandes(page, size);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/demande")
    public DemandeResponse updateDemande(
        Authentication authentication,
        @RequestParam Long demandeId,
        @Valid @RequestBody  DemandeRequest demande
    )    {
        return demandeService.updateDemande(authentication, demandeId, demande);
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/my-demandes/detail")
    public DemandeResponse getDemandeDetail(
            Authentication authentication,
            @RequestParam Long demandeId
    ) {
        return demandeService.getDemandeByIdAndClient(authentication, demandeId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{demandeId}")
    public void deleteDemande(
            @PathVariable Long demandeId
    ) {
        demandeService.deleteDemande(demandeId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{demandeId}/offers")
    public List<OfferResponse> getOffersByDemande(
            @PathVariable Long demandeId
    ) {
        return offerService.getOffersByDemandeId(demandeId);
    }


}
