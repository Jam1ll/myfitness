package com.myfitness.workout_service.controller;

import com.myfitness.workout_service.dto.program.CreateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.UpdateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.WeeklyProgramResponse;
import com.myfitness.workout_service.service.WeeklyProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Tag(name = "Weekly Programs", description = "Endpoints for managing user weekly workout programs")
public class WeeklyProgramController {

    private final WeeklyProgramService weeklyProgramService;

    @Operation(summary = "Create a new weekly program", description = "Schedules routines across days of the week.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Weekly program created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "One or more referenced routines not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeeklyProgramResponse createWeeklyProgram(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CreateWeeklyProgramRequest request) {
        return weeklyProgramService.createWeeklyProgram(userEmail, request);
    }

    @Operation(summary = "Get all weekly programs for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of weekly programs retrieved successfully")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WeeklyProgramResponse> getUserWeeklyPrograms(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail) {
        return weeklyProgramService.getUserWeeklyPrograms(userEmail);
    }

    @Operation(summary = "Get weekly program by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weekly program retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Weekly program not found")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WeeklyProgramResponse getWeeklyProgramById(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the weekly program to retrieve", required = true)
            @PathVariable UUID id) {
        return weeklyProgramService.getWeeklyProgramById(userEmail, id);
    }

    @Operation(summary = "Update an existing weekly program")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weekly program updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Weekly program or referenced routines not found")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WeeklyProgramResponse updateWeeklyProgram(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the weekly program to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWeeklyProgramRequest request) {
        return weeklyProgramService.updateWeeklyProgram(userEmail, id, request);
    }

    @Operation(summary = "Delete a weekly program")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Weekly program deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Weekly program not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWeeklyProgram(
            @Parameter(description = "Authenticated user email injected by API Gateway", required = true)
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "Unique identifier (UUID) of the weekly program to delete", required = true)
            @PathVariable UUID id) {
        weeklyProgramService.deleteWeeklyProgram(userEmail, id);
    }
}