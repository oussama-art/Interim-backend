package com.TroisN.Service.repository;

import com.TroisN.Service.entity.CandidateNoAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateNoAuthRepository extends JpaRepository<CandidateNoAuth, Long> {
}