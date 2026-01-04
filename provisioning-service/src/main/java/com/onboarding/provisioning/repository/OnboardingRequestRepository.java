package com.onboarding.provisioning.repository;

import com.onboarding.provisioning.model.OnboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingRequestRepository extends JpaRepository<OnboardingRequest, String> {
    Optional<OnboardingRequest> findByRequestId(String requestId);
}
