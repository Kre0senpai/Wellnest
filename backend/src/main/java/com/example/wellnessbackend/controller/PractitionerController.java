package com.example.wellnessbackend.controller;

import com.example.wellnessbackend.dto.PractitionerCreateDto;
import com.example.wellnessbackend.dto.PractitionerResponseDto;
import com.example.wellnessbackend.dto.PractitionerUpdateDto;
import com.example.wellnessbackend.entity.Role;
import com.example.wellnessbackend.entity.User;
import com.example.wellnessbackend.repository.UserRepository;
import com.example.wellnessbackend.service.PractitionerService;
import com.example.wellnessbackend.service.TherapySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/practitioners")
@RequiredArgsConstructor
public class PractitionerController {

    private final PractitionerService service;
    private final UserRepository userRepository; // for fetching verified practitioners
    private final TherapySessionService sessionService; // injected to get availability

    @PostMapping("/{userId}")
    public ResponseEntity<PractitionerResponseDto> create(
            @PathVariable Long userId,
            @RequestBody PractitionerCreateDto request) {
        return ResponseEntity.ok(service.createPractitioner(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<PractitionerResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PractitionerResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PractitionerResponseDto> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PractitionerResponseDto> update(
            @PathVariable Long id,
            @RequestBody PractitionerUpdateDto request
    ) {
        return ResponseEntity.ok(service.updatePractitioner(id, request));
    }

    @GetMapping("/verified")
    public ResponseEntity<List<PractitionerResponseDto>> getVerifiedPractitioners() {
        List<User> users = userRepository.findByRoleAndVerified(Role.PRACTITIONER, true);

        List<PractitionerResponseDto> response = users.stream()
                .map(user -> {
                    var practitioner = service.getByUserId(user.getId());
                    return PractitionerResponseDto.builder()
                            .id(practitioner.getId())
                            .userId(user.getId())
                            .specialization(practitioner.getSpecialization())
                            .verified(practitioner.getVerified())
                            .rating(practitioner.getRating())
                            .bio(practitioner.getBio())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ------------------- Get practitioner availability -------------------
    @GetMapping("/{id}/availability")
    public ResponseEntity<List<LocalDateTime>> getAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) String date // optional: filter by date
    ) {
        List<LocalDateTime> freeSlots = sessionService.getAvailableSlots(id, date);
        return ResponseEntity.ok(freeSlots);
    }
}
