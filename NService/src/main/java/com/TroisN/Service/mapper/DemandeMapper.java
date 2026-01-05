package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.demande.DemandeProfilRequest;
import com.TroisN.Service.dto.demande.DemandeProfilResponse;
import com.TroisN.Service.dto.demande.DemandeRequest;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.entity.Demande;
import com.TroisN.Service.entity.DemandeProfil;

import java.util.List;
import java.util.stream.Collectors;

public class DemandeMapper {

    public static Demande toDemandeEntity(DemandeRequest request) {
        Demande demande = new Demande();
        demande.setTitle(request.getTitle());
        demande.setDescription(request.getDescription());
        demande.setTotalEmployeesNeeded(request.getTotalEmployeesNeeded());

        List<DemandeProfil> profilEntities = request.getProfils().stream()
                .map(DemandeMapper::toDemandeProfilEntity)
                .peek(profil -> profil.setDemande(demande))
                .collect(Collectors.toList());

        demande.setProfils(profilEntities);

        return demande;
    }

    public static DemandeProfil toDemandeProfilEntity(DemandeProfilRequest request) {
        DemandeProfil profil = new DemandeProfil();
        profil.setProfilName(request.getProfilName());
        profil.setQuantity(request.getQuantity());
        return profil;
    }

    public static DemandeResponse toDemandeResponse(Demande demande) {
        DemandeResponse response = new DemandeResponse();

        response.setId(demande.getId());
        response.setTitle(demande.getTitle());
        response.setDescription(demande.getDescription());
        response.setTotalEmployeesNeeded(demande.getTotalEmployeesNeeded());
        response.setClientId(demande.getClient().getId());

        List<DemandeProfilResponse> profilResponses = demande.getProfils().stream()
                .map(DemandeMapper::toDemandeProfilResponse)
                .collect(Collectors.toList());

        response.setProfils(profilResponses);

        return response;
    }

    public static DemandeProfilResponse toDemandeProfilResponse(DemandeProfil profil) {
        DemandeProfilResponse response = new DemandeProfilResponse();

        response.setId(profil.getId());
        response.setProfilName(profil.getProfilName());
        response.setQuantity(profil.getQuantity());

        return response;
    }
}
