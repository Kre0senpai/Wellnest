package com.example.wellnessbackend.controller;

import com.example.wellnessbackend.entity.PractitionerProfile;
import com.example.wellnessbackend.entity.Role;
import com.example.wellnessbackend.entity.User;
import com.example.wellnessbackend.repository.PractitionerProfileRepository;
import com.example.wellnessbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PractitionerProfileRepository practitionerProfileRepository;

    // 1️⃣ Get all unverified practitioners
    @GetMapping("/practitioners/unverified")
    public ResponseEntity<?> getUnverifiedPractitioners() {
        List<User> practitioners = userRepository.findByRoleAndVerified(Role.PRACTITIONER, false);
        return ResponseEntity.ok(practitioners);
    }

    // 2️⃣ Verify a practitioner (update both users and practitioner_profile)
    @PutMapping("/practitioner/{id}/verify")
    public ResponseEntity<?> verifyPractitioner(@PathVariable Long id) {
        Optional<User> practitionerOpt = userRepository.findById(id);

        if (practitionerOpt.isEmpty() || practitionerOpt.get().getRole() != Role.PRACTITIONER) {
            return ResponseEntity.status(404).body(Map.of("message", "Practitioner not found"));
        }

        User practitioner = practitionerOpt.get();
        practitioner.setVerified(true);
        userRepository.save(practitioner);

        // Update PractitionerProfile table as well
        Optional<PractitionerProfile> profileOpt = practitionerProfileRepository.findByUserId(practitioner.getId());
        if (profileOpt.isPresent()) {
            PractitionerProfile profile = profileOpt.get();
            profile.setVerified(true);
            practitionerProfileRepository.save(profile);
        }

        return ResponseEntity.ok(Map.of("message", "Practitioner verified successfully"));
    }
}
