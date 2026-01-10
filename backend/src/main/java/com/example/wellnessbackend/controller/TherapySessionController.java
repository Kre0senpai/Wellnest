package com.example.wellnessbackend.controller;

import com.example.wellnessbackend.dto.TherapySessionDto;
import com.example.wellnessbackend.entity.TherapySession;
import com.example.wellnessbackend.service.TherapySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class TherapySessionController {

    private final TherapySessionService sessionService;

    // ------------------- Book a new session -------------------
    @PostMapping("/book")
    public ResponseEntity<?> bookSession(@RequestBody TherapySessionDto dto) {
        try {
            TherapySession session = sessionService.bookSession(dto);
            return ResponseEntity.ok(session);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    // ------------------- Get sessions for a user -------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TherapySession>> getUserSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUser(userId));
    }

    // ------------------- Get sessions for a practitioner -------------------
    @GetMapping("/practitioner/{id}")
    public ResponseEntity<List<TherapySession>> getPractitionerSessions(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionsByPractitioner(id));
    }

    // ------------------- Update session status or notes -------------------
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody TherapySessionDto dto) {
        try {
            TherapySession updated = sessionService.updateSession(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        }
    }

    // ------------------- Cancel session -------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelSession(@PathVariable Long id) {
        try {
            TherapySession cancelled = sessionService.cancelSession(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Session cancelled successfully",
                    "session", cancelled
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/check-slot")
    public ResponseEntity<?> checkSlotAvailability(@RequestBody TherapySessionDto dto) {
        boolean isBooked = sessionService.isSlotBooked(
                dto.getTherapyId(), dto.getPractitionerId(), dto.getDateTime()
        );
        if (isBooked) {
            return ResponseEntity.badRequest().body("Slot already booked");
        } else {
            return ResponseEntity.ok("Slot available");
        }
    }
}
