package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record ProductCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String type,
        @Size(max = 50) String criticality,
        List<Map<String, String>> contacts
) {}
