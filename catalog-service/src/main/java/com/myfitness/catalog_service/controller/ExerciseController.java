package com.myfitness.catalog_service.controller;

import com.myfitness.catalog_service.dto.CreateExerciseRequest;
import com.myfitness.catalog_service.dto.ExerciseResponse;
import com.myfitness.catalog_service.dto.UpdateExerciseRequest;
import com.myfitness.catalog_service.entity.enums.MuscleGroup;
import com.myfitness.catalog_service.service.ExerciseService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercises Catalog", description = "Endpoints for managing the global exercise catalog, searching by muscle group, and internal batch operations")
public class ExerciseController {

    private final ExerciseService exerciseService;

    // CREATE -> POST /api/v1/exercises
    @Operation(
            summary = "Create a new exercise",
            description = "Adds a new exercise to the global catalog with its corresponding muscle group and description."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercise created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(@RequestHeader("X-User-Email") String userEmail,
                                           @RequestHeader("X-User-Name") String userName,
                                           @Valid @RequestBody CreateExerciseRequest request) {
        return exerciseService.createExercise(userEmail,userName, request);
    }

    // UPDATE -> PUT /api/v1/exercises/{id}
    @Operation(
            summary = "Update an existing exercise",
            description = "Updates the name, description, or muscle group of an existing exercise by its UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseResponse updateExercise(
            @Parameter(description = "Unique identifier (UUID) of the exercise to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return exerciseService.updateExercise(id, request);
    }

    // DELETE -> DELETE /api/v1/exercises/{id}
    @Operation(
            summary = "Delete an exercise",
            description = "Permanently deletes an exercise from the catalog by its UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteExercise(
            @Parameter(description = "Unique identifier (UUID) of the exercise to delete", required = true)
            @PathVariable UUID id) {
        return exerciseService.deleteExercise(id);
    }

    // VALIDATE MULTIPLE EXERCISES -> POST /api/v1/exercises/validate
    /* we use POST instead of GET because if there are thousands of IDs,
    the request can exceed the HTTP characters' limit. Thus, passing them through
    the Body is a standard. */
    @Operation(
            summary = "Validate multiple exercise IDs in batch (Internal Microservice Endpoint)",
            description = "Checks in a single database query whether all provided unique exercise UUIDs exist in the catalog."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns true if all exercise IDs exist, false otherwise")
    })
    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public boolean validateExercises(
            @Parameter(description = "List of exercise UUIDs to validate", required = true)
            @RequestBody List<UUID> exerciseIds) {
        return exerciseService.validateExercises(exerciseIds);
    }

    // GET ALL (PAGINATED) -> GET /api/v1/exercises
    @Operation(
            summary = "Get all accessible exercises (Paginated)",
            description = "Retrieves a paginated list of all exercises available for the user in the catalog, with optional search and filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of exercises retrieved successfully")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ExerciseResponse> getAllAccessibleExercises(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "false") boolean mineOnly,
            @ParameterObject Pageable pageable) {
        return exerciseService.getAllAccessibleExercises(userEmail, search, mineOnly, pageable);
    }
    // GET BY ID -> GET /api/v1/exercises/{id}
    @Operation(
            summary = "Get an exercise by ID",
            description = "Retrieves the details of a single exercise by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise found and returned"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseResponse getExerciseById(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the exercise", required = true)
            @PathVariable UUID id) {
        return exerciseService.getExerciseById(id, userEmail);
    }

    // GET BY NAME -> GET /api/v1/exercises/search?name=...
    @Operation(
            summary = "Search an exercise by name",
            description = "Finds a specific exercise matching the provided name query parameter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise found and returned"),
            @ApiResponse(responseCode = "404", description = "Exercise with the specified name not found")
    })
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ExerciseResponse getExerciseByName(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Exact or partial name of the exercise to search", required = true)
            @RequestParam String name) {
        return exerciseService.getExerciseByName(userEmail, name);
    }

    // GET BY MUSCLE GROUP -> GET /api/v1/exercises/muscle-group/{muscleGroup}
    @Operation(
            summary = "Get exercises by muscle group (Paginated)",
            description = "Retrieves a paginated list of exercises filtered by a specific target muscle group (e.g., CHEST, BACK, LEGS)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of filtered exercises retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid muscle group enum value")
    })
    @GetMapping("/muscle-group/{muscleGroup}")
    @ResponseStatus(HttpStatus.OK)
    public Page<ExerciseResponse> getAllExercisesByMuscleGroup(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Target muscle group enum", required = true)
            @PathVariable MuscleGroup muscleGroup,
            @ParameterObject Pageable pageable) {
        return exerciseService.getAllExercisesByMuscleGroup(muscleGroup, userEmail, pageable);
    }

    // GET ALL BY IDS (BATCHING) -> POST /api/v1/exercises/batch
    @Operation(
            summary = "Get multiple exercises by IDs in batch (Internal Microservice Endpoint)",
            description = "Retrieves the full details of multiple exercises in a single query given a list of UUIDs. Used by workout-service for data aggregation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matching exercises retrieved successfully")
    })
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciseResponse> getExercisesByIds(
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "List of exercise UUIDs to fetch", required = true)
            @RequestBody List<UUID> ids) {
        return exerciseService.getExercisesByIds(ids, userEmail);
    }
}