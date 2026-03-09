package com.TroisN.Service.repository;

import com.TroisN.Service.entity.ClientCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientCompany, Long> {
    boolean existsByEmailAddress(String emailAddress);
    Optional<ClientCompany> findBykeycloakUserId(String keycloakId);

    boolean existsByKeycloakUserId(String keycloakUserId);

    void deleteByKeycloakUserId(String keycloakUserId);

    List<ClientCompany> findByNumDemande(Long numDemande);


}