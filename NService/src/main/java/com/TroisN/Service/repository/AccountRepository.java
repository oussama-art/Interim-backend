package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Account;
import com.TroisN.Service.enums.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmailAddress(String emailAddress);

    List<Account> findByStatus(RequestStatus status);

    boolean existsByEmailAddress(String emailAddress);

    @Query("""
    SELECT DISTINCT a FROM Account a
    LEFT JOIN FETCH a.emails""")
    List<Account> findAllWithEmails();

    @EntityGraph(attributePaths = "emails")
    Optional<Account> findWithEmailsById(Long id);

}
