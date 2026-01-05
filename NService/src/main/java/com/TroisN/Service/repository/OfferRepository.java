package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer,Long> {
    @Query("""
    SELECT o FROM Offer o
    JOIN FETCH o.demande
    JOIN FETCH o.client
    LEFT JOIN FETCH o.proposedCandidates pc
    LEFT JOIN FETCH pc.candidate
    WHERE o.id = :offerId
""")
    Optional<Offer> findOfferWithDetails(@Param("offerId") Long offerId);


    @Query("""
        SELECT DISTINCT o FROM Offer o
        JOIN FETCH o.demande
        JOIN FETCH o.client
        LEFT JOIN FETCH o.proposedCandidates pc
        LEFT JOIN FETCH pc.candidate
        WHERE o.client.id = :clientId
    """)
    List<Offer> findOffersByClientIdWithDetails(@Param("clientId") Long clientId);

}
