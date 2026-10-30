package com.myfitness.workout_service.controller;

import com.myfitness.workout_service.dto.routine.CreateRoutineRequest;
import com.myfitness.workout_service.dto.routine.UpdateRoutineRequest;
import com.myfitness.workout_service.dto.routine.RoutineDetailResponse;
import com.myfitness.workout_service.dto.routine.RoutineResponse;
import com.myfitness.workout_service.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/workouts/routines")
@RequiredArgsConstructor
@Tag(name = "Routines", description = "Endpoints for creating, retrieving, updating, and deleting user routines")
public class RoutineController {
    private final RoutineService routineService;

    // CREATE -> POST /api/v1/workouts
    @Operation(
            summary = "Create a new routine",
            description = "Validates all exercise IDs in batch against catalog-service using OpenFeign (protected by Circuit Breaker) and saves the routine."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Routine created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or non-existent exercise IDs"),
            @ApiResponse(responseCode = "503", description = "Catalog service is unavailable (Circuit Breaker triggered)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse createRoutine(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Name") String userName,
            @Valid @RequestBody CreateRoutineRequest request) {
        return routineService.createRoutine(userEmail, userName, request);
    }

    // UPDATE -> PUT /api/v1/workouts/{id}
    @Operation(
            summary = "Update an existing routine",
            description = "Validates the updated list of exercise IDs in batch against catalog-service (protected by Circuit Breaker) and updates the workout routine."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routine updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or non-existent exercise IDs"),
            @ApiResponse(responseCode = "404", description = "Routine routine not found"),
            @ApiResponse(responseCode = "503", description = "Catalog service is unavailable (Circuit Breaker triggered)")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RoutineResponse updateRoutine(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the workout to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoutineRequest request) {
        return routineService.updateRoutine(userEmail, id, request);
    }

    // DELETE -> DELETE /api/v1/workouts/{id}
    @Operation(
            summary = "Delete a workout routine",
            description = "Permanently deletes a workout routine by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Routine deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Routine routine not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteRoutine(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the workout to delete", required = true)
            @PathVariable UUID id) {
        return routineService.deleteRoutine(userEmail, id);
    }

    // GET ALL -> GET /api/v1/workouts
    @Operation(
            summary = "Get all accessible workouts (Paginated)",
            description = "Retrieves a paginated list of all workout routines available, with optional search and filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of workouts retrieved successfully")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RoutineResponse> getAllRoutines(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "false") boolean mineOnly,
            @ParameterObject Pageable pageable) {
        return routineService.getAllAccessibleRoutines(userEmail, search, mineOnly, pageable);
    }

    // GET BY ID (DETAILED VIEW) -> GET /api/v1/workouts/{id}
    @Operation(
            summary = "Get enriched workout detail by ID",
            description = "Retrieves a specific workout verifying user ownership (anti-IDOR) and enriches exercise details in batch from catalog-service. Returns a degraded view if catalog-service is down."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routine detail retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Routine not found or does not belong to the authenticated user")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RoutineDetailResponse getRoutineById(
            @Parameter(description = "Unique identifier (UUID) of the workout to retrieve", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail) {
        return routineService.getRoutineById(id, userEmail);
    }
}