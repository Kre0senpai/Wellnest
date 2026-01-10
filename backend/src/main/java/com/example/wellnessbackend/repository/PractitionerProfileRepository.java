package com.example.wellnessbackend.repository;

import com.example.wellnessbackend.entity.PractitionerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PractitionerProfileRepository extends JpaRepository<PractitionerProfile, Long> {
    Optional<PractitionerProfile> findByUserId(Long userId);
    List<PractitionerProfile> findBySpecializationContainingIgnoreCase(String specialization);
}
