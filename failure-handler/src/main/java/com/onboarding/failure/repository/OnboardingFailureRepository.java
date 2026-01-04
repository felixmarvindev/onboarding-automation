package com.onboarding.failure.repository;

import com.onboarding.failure.model.OnboardingFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingFailureRepository extends JpaRepository<OnboardingFailure, Long> {
    List<OnboardingFailure> findByRequestId(String requestId);
}
