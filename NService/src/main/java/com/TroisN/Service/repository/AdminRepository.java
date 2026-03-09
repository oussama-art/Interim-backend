package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    boolean existsByEmailAddress(String emailAddress);

    @Query("select a.emailAddress from Admin a")
    List<String> findAllAdminEmails();

    Optional<Admin> findByEmailAddress(String emailAddress);


}
