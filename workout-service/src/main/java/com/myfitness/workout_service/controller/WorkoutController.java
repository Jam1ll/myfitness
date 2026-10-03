package com.myfitness.workout_service.controller;

import com.myfitness.workout_service.dto.CreateWorkoutRequest;
import com.myfitness.workout_service.dto.UpdateWorkoutRequest;
import com.myfitness.workout_service.dto.WorkoutDetailResponse;
import com.myfitness.workout_service.dto.WorkoutResponse;
import com.myfitness.workout_service.service.WorkoutService;
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

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "Endpoints for creating, retrieving, updating, and deleting user workout routines")
public class WorkoutController {

    private final WorkoutService workoutService;

    // CREATE -> POST /api/v1/workouts
    @Operation(
            summary = "Create a new workout routine",
            description = "Validates all exercise IDs in batch against catalog-service using OpenFeign (protected by Circuit Breaker) and saves the workout."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or non-existent exercise IDs"),
            @ApiResponse(responseCode = "503", description = "Catalog service is unavailable (Circuit Breaker triggered)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CreateWorkoutRequest request) {
        return workoutService.createWorkout(userEmail, request);
    }

    // UPDATE -> PUT /api/v1/workouts/{id}
    @Operation(
            summary = "Update an existing workout routine",
            description = "Validates the updated list of exercise IDs in batch against catalog-service (protected by Circuit Breaker) and updates the workout routine."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or non-existent exercise IDs"),
            @ApiResponse(responseCode = "404", description = "Workout routine not found"),
            @ApiResponse(responseCode = "503", description = "Catalog service is unavailable (Circuit Breaker triggered)")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WorkoutResponse updateWorkout(
            @Parameter(description = "Unique identifier (UUID) of the workout to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkoutRequest request) {
        return workoutService.updateWorkout(id, request);
    }

    // DELETE -> DELETE /api/v1/workouts/{id}
    @Operation(
            summary = "Delete a workout routine",
            description = "Permanently deletes a workout routine by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workout deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workout routine not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteWorkout(
            @Parameter(description = "Unique identifier (UUID) of the workout to delete", required = true)
            @PathVariable UUID id) {
        return workoutService.deleteWorkout(id);
    }

    // GET BY USER'S EMAIL -> GET /api/v1/workouts
    @Operation(
            summary = "Get all workouts for the authenticated user (Paginated)",
            description = "Retrieves a paginated summary list of all workout routines belonging to the authenticated user's email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of workouts retrieved successfully")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<WorkoutResponse> getAllWorkoutsByUserEmail(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @ParameterObject Pageable pageable) {
        return workoutService.getAllWorkoutsByUserEmail(userEmail, pageable);
    }

    // GET BY ID (DETAILED VIEW) -> GET /api/v1/workouts/{id}
    @Operation(
            summary = "Get enriched workout detail by ID",
            description = "Retrieves a specific workout verifying user ownership (anti-IDOR) and enriches exercise details in batch from catalog-service. Returns a degraded view if catalog-service is down."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout detail retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workout not found or does not belong to the authenticated user")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WorkoutDetailResponse getWorkoutById(
            @Parameter(description = "Unique identifier (UUID) of the workout to retrieve", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail) {
        return workoutService.getWorkoutById(id, userEmail);
    }
}