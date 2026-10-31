package com.myfitness.workout_service.service;

import com.myfitness.workout_service.dto.program.CreateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.ProgramDayItemDto;
import com.myfitness.workout_service.dto.program.UpdateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.WeeklyProgramResponse;
import com.myfitness.workout_service.entity.program.ProgramDayEntity;
import com.myfitness.workout_service.entity.program.WeeklyProgramEntity;
import com.myfitness.workout_service.entity.routine.RoutineEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.mapper.WeeklyProgramMapper;
import com.myfitness.workout_service.repository.RoutineRepository;
import com.myfitness.workout_service.repository.WeeklyProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyProgramService {

    private final WeeklyProgramRepository weeklyProgramRepository;
    private final RoutineRepository routineRepository;
    private final WeeklyProgramMapper weeklyProgramMapper;

    //CREATE
    @Transactional
    public WeeklyProgramResponse createWeeklyProgram(String userEmail, CreateWeeklyProgramRequest request) {
        WeeklyProgramEntity programEntity = weeklyProgramMapper.toEntity(request);
        programEntity.setUserEmail(userEmail);

        for (ProgramDayItemDto dayItem : request.programDays()) {
            RoutineEntity routine = routineRepository.findById(dayItem.routineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Routine with id '" + dayItem.routineId() + "' not found."));

            // Aquí verificamos que el usuario tenga acceso a la rutina que intenta agregar.
            if (routine.isRoutinePrivate() && !routine.getUserEmail().equals(userEmail)) {
                throw new ResourceNotFoundException("Routine with id '" + dayItem.routineId() + "' not found or you don't have access.");
            }

            ProgramDayEntity programDay = ProgramDayEntity.builder()
                    .routineId(routine.getId())
                    .dayOfWeek(dayItem.dayOfWeek())
                    .build();
            programEntity.addProgramDay(programDay);
        }

        WeeklyProgramEntity savedEntity = weeklyProgramRepository.save(programEntity);
        return weeklyProgramMapper.toResponse(savedEntity, userEmail);
    }

    //UPDATE
    @Transactional
    public WeeklyProgramResponse updateWeeklyProgram(String userEmail, UUID id, UpdateWeeklyProgramRequest request) {
        WeeklyProgramEntity existingProgram = weeklyProgramRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly Program with id '" + id + "' not found"));

        existingProgram.setName(request.name());

        //clean actual program days in order to add the new ones
        existingProgram.getProgramDayEntities().clear();

        for (ProgramDayItemDto dayItem : request.programDays()) {
            RoutineEntity routine = routineRepository.findById(dayItem.routineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Routine with id '" + dayItem.routineId() + "' not found."));

            if (routine.isRoutinePrivate() && !routine.getUserEmail().equals(userEmail)) {
                throw new ResourceNotFoundException("Routine with id '" + dayItem.routineId() + "' not found or you don't have access.");
            }

            ProgramDayEntity programDay = ProgramDayEntity.builder()
                    .routineId(routine.getId())
                    .dayOfWeek(dayItem.dayOfWeek())
                    .build();
            existingProgram.addProgramDay(programDay);
        }

        WeeklyProgramEntity savedEntity = weeklyProgramRepository.save(existingProgram);
        return weeklyProgramMapper.toResponse(savedEntity, userEmail);
    }

    //DELETE
    @Transactional
    public void deleteWeeklyProgram(String userEmail, UUID id) {
        WeeklyProgramEntity weeklyProgram = weeklyProgramRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly Program with id '" + id + "' not found"));
        weeklyProgramRepository.delete(weeklyProgram);
    }

    //GET ALL ACCESSIBLE
    public List<WeeklyProgramResponse> getUserWeeklyPrograms(String userEmail) {
        List<WeeklyProgramEntity> programs = weeklyProgramRepository.findAllByUserEmail(userEmail);
        return programs.stream()
                .map(program -> weeklyProgramMapper.toResponse(program, userEmail))
                .collect(Collectors.toList());
    }

    //GET BY ID
    public WeeklyProgramResponse getWeeklyProgramById(String userEmail, UUID id) {
        WeeklyProgramEntity program = weeklyProgramRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly Program with id '" + id + "' not found"));
        return weeklyProgramMapper.toResponse(program, userEmail);
    }

    //GET BY ACTIVE
    public WeeklyProgramResponse getActiveUserWeeklyProgram(UUID id, String userEmail){
        WeeklyProgramEntity program = weeklyProgramRepository.findByIdAndUserEmailAndWeeklyProgramActiveTrue(id, userEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Active Weekly Program not found"));
        return weeklyProgramMapper.toResponse(program, userEmail);
    }
}