package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Demande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande,Long> {
    Page<Demande> findByClient_Id(Long clientId, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT d FROM Demande d LEFT JOIN FETCH d.profils",
            countQuery = "SELECT COUNT(d) FROM Demande d"
    )
    Page<Demande> findAllWithProfils(Pageable pageable);

    Optional<Demande> findByIdAndClientId(Long demandeId, Long clientId);


}
