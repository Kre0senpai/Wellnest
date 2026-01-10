package com.example.wellnessbackend.repository;

import com.example.wellnessbackend.entity.TherapySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {

    // Get all sessions for a user
    List<TherapySession> findByUserId(Long userId);

    // Get all sessions for a practitioner
    List<TherapySession> findByPractitionerId(Long practitionerId);

    // ✅ Check if a therapy slot is already booked for a specific practitioner at a given date/time
    boolean existsByTherapyIdAndPractitionerIdAndDateTime(Long therapyId, Long practitionerId, LocalDateTime dateTime);
}
