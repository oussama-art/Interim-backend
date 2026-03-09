package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByDemandeId(Long demandeId);

    List<Contract> findByCandidateId(Long candidateId);

    Optional<Contract> findByCandidateIdAndDemandeId(Long candidateId, Long demandeId);

    Optional<Contract> findByIdAndDemandeId(Long id, Long demandeId);

    @Query("""
        select max(c.endDate)
        from Contract c
        where c.candidate.id = :candidateId
    """)
    Optional<LocalDate> findLastEndDateByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
        select count(c) > 0
        from Contract c
        where c.candidate.id = :candidateId
          and c.startDate <= :endDate
          and c.endDate >= :startDate
          and (:excludeId is null or c.id <> :excludeId)
    """)
    boolean existsOverlappingContract(
            @Param("candidateId") Long candidateId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );

    @Query("""
    select c
    from Contract c
    where c.candidate.id = :candidateId
      and c.startDate <= :endDate
      and c.endDate >= :startDate
    order by c.startDate asc
""")
    List<Contract> findOverlappingContracts(
            @Param("candidateId") Long candidateId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}