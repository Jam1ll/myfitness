package com.myfitness.workout_service.service;

import com.myfitness.workout_service.dto.program.CreateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.ProgramDayItemDto;
import com.myfitness.workout_service.dto.program.UpdateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.WeeklyProgramResponse;
import com.myfitness.workout_service.entity.enums.DayOfWeek;
import com.myfitness.workout_service.entity.program.WeeklyProgramEntity;
import com.myfitness.workout_service.entity.routine.RoutineEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.mapper.WeeklyProgramMapper;
import com.myfitness.workout_service.repository.RoutineRepository;
import com.myfitness.workout_service.repository.WeeklyProgramRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyProgramServiceTest {

    @Mock
    private WeeklyProgramRepository weeklyProgramRepository;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private WeeklyProgramMapper weeklyProgramMapper;

    @InjectMocks
    private WeeklyProgramService weeklyProgramService;

    @Test
    @DisplayName("Should create weekly program when routines exist and belong to user")
    void createWeeklyProgram_WhenRoutinesValid_ShouldReturnResponse() {
        String userEmail = "test@gmail.com";
        UUID routineId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        List<ProgramDayItemDto> days = List.of(
                new ProgramDayItemDto(routineId, DayOfWeek.MONDAY)
        );
        // Corregido: Se agregaron los 3 argumentos que exige CreateWeeklyProgramRequest
        CreateWeeklyProgramRequest request = new CreateWeeklyProgramRequest("PPL Program", days, true);

        RoutineEntity routineEntity = RoutineEntity.builder()
                .id(routineId)
                .userEmail(userEmail)
                .routinePrivate(true)
                .build();

        WeeklyProgramEntity programEntity = new WeeklyProgramEntity();
        WeeklyProgramEntity savedEntity = new WeeklyProgramEntity();
        savedEntity.setId(programId);

        // Corregido: Orden y tipos exactos para WeeklyProgramResponse (id, name, createdBy, createdAt, programOwner, programDays, weeklyProgramActive)
        WeeklyProgramResponse expectedResponse = new WeeklyProgramResponse(
                programId, "PPL Program", "JohnDoe", LocalDateTime.MIN, true, List.of(), true
        );

        when(weeklyProgramMapper.toEntity(request)).thenReturn(programEntity);
        when(routineRepository.findById(routineId)).thenReturn(Optional.of(routineEntity));
        when(weeklyProgramRepository.save(programEntity)).thenReturn(savedEntity);
        when(weeklyProgramMapper.toResponse(savedEntity, userEmail)).thenReturn(expectedResponse);

        WeeklyProgramResponse actual = weeklyProgramService.createWeeklyProgram(userEmail, request);

        assertNotNull(actual);
        assertEquals(programId, actual.id());
        verify(routineRepository, times(1)).findById(routineId);
        verify(weeklyProgramRepository, times(1)).save(programEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to add a private routine from another user")
    void createWeeklyProgram_WhenRoutineIsPrivateAndNotOwned_ShouldThrowException() {
        String userEmail = "test@gmail.com";
        UUID routineId = UUID.randomUUID();

        List<ProgramDayItemDto> days = List.of(new ProgramDayItemDto(routineId, DayOfWeek.MONDAY));
        CreateWeeklyProgramRequest request = new CreateWeeklyProgramRequest("My Program", days, true);

        RoutineEntity privateRoutineOtherUser = RoutineEntity.builder()
                .id(routineId)
                .userEmail("other@gmail.com")
                .routinePrivate(true)
                .build();

        // ¡Añade esta línea para que devuelva una entidad y no falle por null!
        when(weeklyProgramMapper.toEntity(request)).thenReturn(new WeeklyProgramEntity());
        when(routineRepository.findById(routineId)).thenReturn(Optional.of(privateRoutineOtherUser));

        assertThrows(ResourceNotFoundException.class,
                () -> weeklyProgramService.createWeeklyProgram(userEmail, request));

        verify(weeklyProgramRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return list of programs for a specific user")
    void getUserWeeklyPrograms_ShouldReturnListOfResponses() {
        String userEmail = "test@gmail.com";
        WeeklyProgramEntity entity = new WeeklyProgramEntity();
        // Corregido: Orden y tipos exactos para WeeklyProgramResponse
        WeeklyProgramResponse response = new WeeklyProgramResponse(
                UUID.randomUUID(), "Program 1", "John", LocalDateTime.MIN, true, List.of(), true);

        when(weeklyProgramRepository.findAllByUserEmail(userEmail)).thenReturn(List.of(entity));
        when(weeklyProgramMapper.toResponse(entity, userEmail)).thenReturn(response);

        List<WeeklyProgramResponse> result = weeklyProgramService.getUserWeeklyPrograms(userEmail);

        assertEquals(1, result.size());
        assertEquals("Program 1", result.getFirst().name());
        verify(weeklyProgramRepository, times(1)).findAllByUserEmail(userEmail);
    }

    @Test
    @DisplayName("Should update an existing program when valid request is provided")
    void updateWeeklyProgram_WhenValid_ShouldUpdateAndReturnResponse() {
        String userEmail = "test@gmail.com";
        UUID programId = UUID.randomUUID();
        UUID routineId =UUID.randomUUID();

        UpdateWeeklyProgramRequest request = new UpdateWeeklyProgramRequest(
                "Updated Program",
                List.of(new ProgramDayItemDto(routineId, DayOfWeek.FRIDAY)),
                true
        );

        WeeklyProgramEntity existingProgram = WeeklyProgramEntity.builder()
                .id(programId)
                .programDayEntities(new ArrayList<>())
                .build();

        RoutineEntity routineEntity = RoutineEntity.builder()
                .id(routineId)
                .userEmail(userEmail)
                .build();

        // Corregido: Orden y tipos exactos para WeeklyProgramResponse
        WeeklyProgramResponse expectedResponse = new WeeklyProgramResponse(
                programId, "Updated Program", "John", LocalDateTime.MIN, true, List.of(), true);

        when(weeklyProgramRepository.findByIdAndUserEmail(programId, userEmail))
                .thenReturn(Optional.of(existingProgram));
        when(routineRepository.findById(routineId)).thenReturn(Optional.of(routineEntity));
        when(weeklyProgramRepository.save(existingProgram)).thenReturn(existingProgram);
        when(weeklyProgramMapper.toResponse(existingProgram, userEmail)).thenReturn(expectedResponse);

        WeeklyProgramResponse actual = weeklyProgramService.updateWeeklyProgram(userEmail, programId, request);

        assertNotNull(actual);
        assertEquals("Updated Program", existingProgram.getName());
        assertEquals(1, existingProgram.getProgramDayEntities().size());
        verify(weeklyProgramRepository, times(1)).save(existingProgram);
    }

    @Test
    @DisplayName("Should delete weekly program when ID exists and belongs to user")
    void deleteWeeklyProgram_WhenExists_ShouldDeleteEntity() {
        UUID programId =UUID.randomUUID();
        String userEmail = "test@gmail.com";

        WeeklyProgramEntity mockProgram = WeeklyProgramEntity.builder()
                .id(programId)
                .userEmail(userEmail)
                .build();

        when(weeklyProgramRepository.findByIdAndUserEmail(programId, userEmail))
                .thenReturn(Optional.of(mockProgram));

        weeklyProgramService.deleteWeeklyProgram(userEmail, programId);

        verify(weeklyProgramRepository, times(1)).findByIdAndUserEmail(programId, userEmail);
        verify(weeklyProgramRepository, times(1)).delete(mockProgram);
    }
}