package com.example.wellnessbackend.service;

import com.example.wellnessbackend.dto.TherapySessionDto;
import com.example.wellnessbackend.entity.TherapySession;
import com.example.wellnessbackend.repository.TherapySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TherapySessionService {

    private final TherapySessionRepository sessionRepository;
    private final NotificationService notificationService; // ✅ Inject NotificationService

    // ------------------- Book a new therapy session -------------------
    public TherapySession bookSession(TherapySessionDto dto) {

        // 1️⃣ Reject non-hour aligned bookings (15:30, 10:15 etc.)
        if (dto.getDateTime().getMinute() != 0) {
            throw new RuntimeException("Invalid slot. Please select a valid time slot.");
        }

        // 2️⃣ Ensure slot exists in availability
        List<LocalDateTime> availableSlots = getAvailableSlots(
                dto.getPractitionerId(),
                dto.getDateTime().toLocalDate().toString()
        );

        if (!availableSlots.contains(dto.getDateTime())) {
            throw new RuntimeException("Selected slot is no longer available");
        }

        // 3️⃣ Final safety check at DB level (race condition protection)
        if (sessionRepository.existsByTherapyIdAndPractitionerIdAndDateTime(
                dto.getTherapyId(),
                dto.getPractitionerId(),
                dto.getDateTime())) {
            throw new RuntimeException("This slot is already booked");
        }

        // 4️⃣ Create & save session
        TherapySession session = TherapySession.builder()
                .therapyId(dto.getTherapyId())
                .practitionerId(dto.getPractitionerId())
                .userId(dto.getUserId())
                .dateTime(dto.getDateTime())
                .status("booked")
                .notes(dto.getNotes())
                .build();

        session = sessionRepository.save(session);

        // 5️⃣ Send notifications
        notificationService.createNotification(
                session.getUserId(),
                "SESSION_BOOKED",
                "Your session with practitioner " + session.getPractitionerId() +
                        " is booked for " + session.getDateTime()
        );
        notificationService.createNotification(
                session.getPractitionerId(),
                "SESSION_BOOKED",
                "You have a new session booked with user " + session.getUserId() +
                        " at " + session.getDateTime()
        );

        return session;
    }

    // ------------------- Get all sessions for a user -------------------
    public List<TherapySession> getSessionsByUser(Long userId) {
        return sessionRepository.findByUserId(userId);
    }

    // ------------------- Get all sessions for a practitioner -------------------
    public List<TherapySession> getSessionsByPractitioner(Long practitionerId) {
        return sessionRepository.findByPractitionerId(practitionerId);
    }

    // ------------------- Update session status or notes -------------------
    public TherapySession updateSession(Long sessionId, TherapySessionDto dto) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        if (dto.getStatus() != null) {
            session.setStatus(dto.getStatus());

            // ✅ Send notifications for completed sessions
            if ("completed".equalsIgnoreCase(dto.getStatus())) {
                notificationService.createNotification(
                        session.getUserId(),
                        "SESSION_COMPLETED",
                        "Your session with practitioner " + session.getPractitionerId() +
                                " on " + session.getDateTime() + " is completed"
                );
                notificationService.createNotification(
                        session.getPractitionerId(),
                        "SESSION_COMPLETED",
                        "You completed the session with user " + session.getUserId() +
                                " on " + session.getDateTime()
                );
            }
        }

        if (dto.getNotes() != null) session.setNotes(dto.getNotes());

        return sessionRepository.save(session);
    }

    // ------------------- Cancel a session -------------------
    public TherapySession cancelSession(Long sessionId) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        if ("completed".equalsIgnoreCase(session.getStatus())) {
            throw new RuntimeException("Session already completed and cannot be cancelled");
        }

        session.setStatus("cancelled");
        session = sessionRepository.save(session);

        // ✅ Send notifications for cancellation
        notificationService.createNotification(
                session.getUserId(),
                "SESSION_CANCELLED",
                "Your session with practitioner " + session.getPractitionerId() +
                        " on " + session.getDateTime() + " has been cancelled"
        );
        notificationService.createNotification(
                session.getPractitionerId(),
                "SESSION_CANCELLED",
                "Session with user " + session.getUserId() +
                        " on " + session.getDateTime() + " has been cancelled"
        );

        return session;
    }

    // ------------------- Get available slots for a practitioner -------------------
    public List<LocalDateTime> getAvailableSlots(Long practitionerId, String dateStr) {
        LocalDate date;
        if (dateStr != null) {
            date = LocalDate.parse(dateStr);
        } else {
            date = LocalDate.now();
        }

        List<LocalDateTime> allSlots = new ArrayList<>();
        for (int hour = 9; hour < 17; hour++) {
            allSlots.add(LocalDateTime.of(date, LocalTime.of(hour, 0)));
        }

        List<TherapySession> sessions = sessionRepository.findByPractitionerId(practitionerId);

        sessions.stream()
                .filter(s -> s.getDateTime().toLocalDate().equals(date))
                .forEach(s -> allSlots.remove(s.getDateTime()));

        return allSlots;
    }

    // ------------------- Check if slot is booked -------------------
    public boolean isSlotBooked(Long therapyId, Long practitionerId, LocalDateTime dateTime) {
        return sessionRepository.existsByTherapyIdAndPractitionerIdAndDateTime(
                therapyId, practitionerId, dateTime
        );
    }
}
