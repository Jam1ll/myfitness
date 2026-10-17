package com.myfitness.workout_service.client;

import com.myfitness.workout_service.dto.ExerciseSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//OpenFeign implementation (internal communication within microservices)
@FeignClient(name = "catalog-service", url = "http://catalog-service:8080")
public interface CatalogClient {

    @GetMapping("/api/v1/exercises/{id}")
    ExerciseSummaryDto getExerciseById(@PathVariable("id") UUID id);

    @PostMapping("/api/v1/exercises/batch")
    List<ExerciseSummaryDto> getExercisesByIds(@RequestHeader("X-User-Email") String userEmail, @RequestBody List<UUID> exercisesIds);

    @PostMapping("/api/v1/exercises/validate")
    boolean validateExercises(@RequestBody List<UUID> exerciseIds);
}
