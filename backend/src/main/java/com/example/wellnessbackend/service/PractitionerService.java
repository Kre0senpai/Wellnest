package com.example.wellnessbackend.service;

import com.example.wellnessbackend.dto.PractitionerCreateDto;
import com.example.wellnessbackend.dto.PractitionerResponseDto;
import com.example.wellnessbackend.dto.PractitionerUpdateDto;
import com.example.wellnessbackend.entity.PractitionerProfile;
import com.example.wellnessbackend.entity.Role;
import com.example.wellnessbackend.entity.User;
import com.example.wellnessbackend.repository.PractitionerProfileRepository;
import com.example.wellnessbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PractitionerService {

    private final PractitionerProfileRepository repository;
    private final UserRepository userRepository;

    public PractitionerResponseDto createPractitioner(Long userId, PractitionerCreateDto request) {
        // ✅ Step 1: Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Step 2: Check if user role is PRACTITIONER
        if (user.getRole() != Role.PRACTITIONER) {
            throw new RuntimeException("Cannot create practitioner profile: User is not a practitioner");
        }

        // ✅ Step 3: Check if practitioner profile already exists
        if (repository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Practitioner profile already exists for this user");
        }

        // ✅ Step 4: Create practitioner profile
        PractitionerProfile profile = PractitionerProfile.builder()
                .userId(userId)
                .specialization(request.getSpecialization())
                .bio(request.getBio() != null ? request.getBio() : user.getBio()) // fallback to user's bio
                .verified(false)
                .rating(0.0)
                .build();

        PractitionerProfile saved = repository.save(profile);

        return mapToDto(saved);
    }

    public List<PractitionerResponseDto> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public PractitionerResponseDto getById(Long id) {
        PractitionerProfile p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Practitioner not found with id: " + id));
        return mapToDto(p);
    }

    public PractitionerResponseDto getByUserId(Long userId) {
        PractitionerProfile p = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Practitioner not found for userId: " + userId));
        return mapToDto(p);
    }

    public PractitionerResponseDto updatePractitioner(Long id, PractitionerUpdateDto request) {
        PractitionerProfile profile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Practitioner not found"));

        // Update fields
        profile.setSpecialization(request.getSpecialization());
        profile.setBio(request.getBio());

        PractitionerProfile updated = repository.save(profile);

        return mapToDto(updated);
    }

    private PractitionerResponseDto mapToDto(PractitionerProfile p) {
        return PractitionerResponseDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .specialization(p.getSpecialization())
                .bio(p.getBio())
                .verified(p.getVerified())
                .rating(p.getRating())
                .build();
    }
}
