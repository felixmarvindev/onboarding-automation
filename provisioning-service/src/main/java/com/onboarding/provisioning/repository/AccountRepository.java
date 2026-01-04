package com.onboarding.provisioning.repository;

import com.onboarding.provisioning.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByRequestId(String requestId);
    Optional<Account> findByAccountId(String accountId);
}
